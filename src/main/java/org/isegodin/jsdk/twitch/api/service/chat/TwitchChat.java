package org.isegodin.jsdk.twitch.api.service.chat;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author isegodin
 */
public class TwitchChat {

    private static final String IRC_HOST = "irc.chat.twitch.tv";
    private static final int IRC_SSL_PORT = 443;
    private static final int IRC_PORT = 6667;

    private final Object monitor = new Object();

    private final InternetRelayChat relayChat;

    private volatile String joinedChannel;

    public TwitchChat(String userName, String accessToken) {
        this.relayChat = new InternetRelayChat(IRC_HOST, IRC_PORT, TwitchChat.this::internalMessageListener);
        this.relayChat.start();

        relayChat.sendCommand("PASS oauth:" + accessToken);
        relayChat.sendCommand("NICK " + userName);


    }

    public void joinChannel(String channel) {
        synchronized (monitor) {
            relayChat.sendCommand("JOIN #" + channel);
            joinedChannel = channel;
        }
    }

    public void sendMessage(String message) {
        relayChat.sendCommand("PRIVMSG #" + joinedChannel + " :" + message);
    }


    /**
     * @deprecated remove, for test only
     * @param millis
     * @param description
     */
    @Deprecated
    public void waitForMessage(long millis, String description) {
        relayChat.waitForResponse(s -> s.contains(" PRIVMSG "), millis, description);
    }

    public void onMessageReceive(Consumer<Object> consumer) {
        // TODO
    }

    public void onUserJoin(Consumer<Object> consumer) {
        // TODO
    }

    public void onUserPart(Consumer<Object> consumer) {
        // TODO
    }

    public void onSubscribe(Consumer<Object> consumer) {
        // TODO
    }

    private void internalMessageListener(String message) {
        Pattern pingPattern = Pattern.compile("PING\\s(?<argument>.+)");
        Matcher matcher = pingPattern.matcher(message);
        if (matcher.matches()) {
            String pingArgument = matcher.group("argument");
            relayChat.sendCommand("PONG " + pingArgument);
        } else {
            // TODO proper handling (listeners)
        }
    }
}


