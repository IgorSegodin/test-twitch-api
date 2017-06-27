package org.isegodin.jsdk.twitch.api.service.chat;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author isegodin
 */
public class TwitchChat {

    private static final long DEFAULT_TIMEOUT_MILLIS = 10_000;

    private static final String IRC_HOST = "irc.chat.twitch.tv";
    private static final int IRC_SSL_PORT = 443;
    private static final int IRC_PORT = 6667;

    private static final Pattern AUTH_SUCCESS_PATTERN = Pattern.compile("^\\S+\\s004\\s.*$");
    private static final Pattern JOIN_SUCCESS_PATTERN = Pattern.compile("^:(?<name>.+)!\\S+\\sJOIN\\s.*$");
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^:(?<name>.+)!\\S+\\sPRIVMSG\\s#(?<channel>\\S+)\\s:(?<message>.*)$");

    private final Object monitor = new Object();

    private final InternetRelayChat relayChat;
    private final String userName;

    private volatile String joinedChannel;

    public TwitchChat(String userName, String accessToken) {
        this.userName = userName;
        this.relayChat = new InternetRelayChat(IRC_HOST, IRC_PORT, TwitchChat.this::internalMessageListener);
        this.relayChat.start();

        this.relayChat.sendCommandAndWaitForResponse(
                "PASS oauth:" + accessToken + System.lineSeparator() + "NICK " + userName,
                (message) -> AUTH_SUCCESS_PATTERN.matcher(message).matches(),
                DEFAULT_TIMEOUT_MILLIS,
                "authenticating"
        );
    }

    public void joinChannel(String channel) {
        synchronized (monitor) {
            relayChat.sendCommandAndWaitForResponse(
                    "JOIN #" + channel,
                    (message) -> JOIN_SUCCESS_PATTERN.matcher(message).matches(),
                    DEFAULT_TIMEOUT_MILLIS,
                    "joining channel '" + channel + "'"
            );
            joinedChannel = channel;
        }
    }

    public void sendMessage(String message) {
        relayChat.sendCommand("PRIVMSG #" + joinedChannel + " :" + message);
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
        Matcher pingMatcher = pingPattern.matcher(message);
        if (pingMatcher.matches()) {
            String pingArgument = pingMatcher.group("argument");
            relayChat.sendCommand("PONG " + pingArgument);
        } else {
            // TODO proper handling (listeners)
//            Matcher msgMatcher = MESSAGE_PATTERN.matcher(message);
//            if (msgMatcher.matches()) {
//                System.out.println(msgMatcher.group("name") + ": " + msgMatcher.group("message"));
//            }
        }
    }
}


