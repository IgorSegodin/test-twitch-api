package org.isegodin.jsdk.twitch.api.service.chat;

import lombok.extern.log4j.Log4j2;
import org.isegodin.jsdk.twitch.api.service.chat.event.TextMessage;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author isegodin
 */
@Log4j2
public class TwitchChat implements ChatApi {

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
    private volatile ChatEventListener eventListener;

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

    public void joinChannel(String channel, ChatEventListener eventListener) {
        synchronized (monitor) {
            relayChat.sendCommandAndWaitForResponse(
                    "JOIN #" + channel,
                    (message) -> JOIN_SUCCESS_PATTERN.matcher(message).matches(),
                    DEFAULT_TIMEOUT_MILLIS,
                    "joining channel '" + channel + "'"
            );
            joinedChannel = channel;
            this.eventListener = eventListener;
        }
    }

    @Override
    public void sendMessage(String message) {
        relayChat.sendCommand("PRIVMSG #" + joinedChannel + " :" + message);
    }

    private void internalMessageListener(String message) {
        Pattern pingPattern = Pattern.compile("PING\\s(?<argument>.+)");
        Matcher pingMatcher = pingPattern.matcher(message);
        if (pingMatcher.matches()) {
            String pingArgument = pingMatcher.group("argument");
            relayChat.sendCommand("PONG " + pingArgument);
        } else {
            log.info("Message received: {}", message);
            Matcher msgMatcher = MESSAGE_PATTERN.matcher(message);
            if (!msgMatcher.matches() || !Objects.equals(joinedChannel, msgMatcher.group("channel"))) {
                return;
            }
            if (this.eventListener != null) {
                TextMessage textMessage = TextMessage.builder()
                        .username(msgMatcher.group("name"))
                        .message(msgMatcher.group("message"))
                        .build();
                try {
                    this.eventListener.onTextMessageReceive(textMessage,
                            this);
                } catch (Exception e) {
                    log.error("Listener error " + textMessage, e);
                }
            }
        }
    }
}


