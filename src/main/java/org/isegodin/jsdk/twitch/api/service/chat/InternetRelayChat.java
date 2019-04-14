package org.isegodin.jsdk.twitch.api.service.chat;

import lombok.extern.log4j.Log4j2;
import org.isegodin.jsdk.twitch.api.util.Stopwatch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author isegodin
 */
@Log4j2
public class InternetRelayChat {

    private final String host;
    private final int port;
    private final Consumer<String> messageListener;

    private volatile Socket socket;
    private volatile Receiver receiver;
    private volatile Waiter waiter;
    private volatile Sender sender;

    public InternetRelayChat(String host, int port, Consumer<String> messageListener) {
        this.host = host;
        this.port = port;
        this.messageListener = messageListener;
    }

    public void start() {
        if (socket != null) {
            return;
        }
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        receiver = new Receiver(socket, InternetRelayChat.this::onReceive);
        receiver.start();

        waiter = new Waiter();

        sender = new Sender(socket);
    }

    public void stop() {
        receiver.interrupt();
        receiver = null;

        waiter.stop();
        waiter = null;

        sender = null;

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Socket close error", e);
            }
            socket = null;
        }
    }

    public void sendCommand(String command) {
        sender.send(command);
        log.info("Sent command: {}", command);
    }

    public void sendCommandAndWaitForResponse(String command, Predicate<String> predicate, long millis, String description) {
        __internalWaitForResponse__(() -> {
            sendCommand(command);
        }, predicate, millis, description);
    }

    public void waitForResponse(Predicate<String> predicate, long millis, String description) {
        __internalWaitForResponse__(null, predicate, millis, description);
    }

    private void __internalWaitForResponse__(Callback callback, Predicate<String> predicate, long millis, String description) {
        Object monitor = new Object();

        WaitPredicate waitPredicate = new WaitPredicate(predicate, monitor);

        waiter.addWaitCondition(waitPredicate);

        if (callback != null) {
            callback.call();
        }

        synchronized (monitor) {
            Stopwatch stopwatch = Stopwatch.start();
            try {
                monitor.wait(millis);
            } catch (InterruptedException e) {
                //
            }
            if (stopwatch.isElapsedMillis(millis)) {
                waiter.removeWaitCondition(waitPredicate);
                throw new RuntimeException("Timeout for waiting: " + description);
            }
        }
    }

    protected void onReceive(String command) {
        log.info("Received: {}", command);
        waiter.checkPredicates(command);
        messageListener.accept(command);
    }

    private interface Callback {
        void call();
    }

    /**
     * Holds waiting condition and synchronization monitor.
     */
    private static class WaitPredicate {

        private final Predicate<String> predicate;
        private final Object monitor;

        public WaitPredicate(Predicate<String> predicate, Object monitor) {
            this.predicate = predicate;
            this.monitor = monitor;
        }

        public Predicate<String> getPredicate() {
            return predicate;
        }

        public Object getMonitor() {
            return monitor;
        }
    }


    /**
     * Manages waiting conditions and callbacks.
     *
     */
    private static class Waiter {

        private volatile boolean running = true;

        private final ConcurrentHashMap<Predicate<String>, WaitPredicate> predicateMap = new ConcurrentHashMap<>();

        public void stop() {
            running = false;
        }

        public void checkPredicates(final String command) {
            Iterator<WaitPredicate> iterator = predicateMap.values().iterator();
            // TODO probably need to change this with thread pool or with single thread and message queue
            new Thread(() -> {
                while (running && iterator.hasNext()) {
                    WaitPredicate next = iterator.next();
                    try {
                        if (next.getPredicate().test(command)) {
                            iterator.remove();
                            synchronized (next.getMonitor()) {
                                next.getMonitor().notify();
                            }
                        }
                    } catch (Exception e) {
                        log.error("Waiter error", e);
                    }
                }
            })
                    .start();
        }

        public void addWaitCondition(WaitPredicate predicate) {
            predicateMap.put(predicate.getPredicate(), predicate);
        }

        public void removeWaitCondition(WaitPredicate predicate) {
            predicateMap.remove(predicate.getPredicate());
        }
    }

    /**
     * Separate thread for receiving messages from socket input stream.
     */
    private static class Receiver extends Thread {

        private final Consumer<String> consumer;
        private final BufferedReader reader;

        public Receiver(Socket socket, Consumer<String> consumer) {
            this.consumer = consumer;
            try {
                this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            try {
                String line;
                while (!isInterrupted() && (line = reader.readLine()) != null) {
                    consumer.accept(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Wrap socket output stream, add line separator and flush after each command.
     */
    private static class Sender {

        private final BufferedWriter writer;

        public Sender(Socket socket) {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void send(String command) {
            try {
                writer.write(command);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
