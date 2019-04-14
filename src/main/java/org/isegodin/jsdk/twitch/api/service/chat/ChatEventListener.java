package org.isegodin.jsdk.twitch.api.service.chat;

import org.isegodin.jsdk.twitch.api.service.chat.event.TextMessage;

import java.util.function.Consumer;

/**
 * @author isegodin
 */
public interface ChatEventListener {

    default void onTextMessageReceive(TextMessage textMessage, ChatApi chatApi) {
    }

//    default void onUserJoin(Consumer<Object> consumer) {
//    }
//
//    default void onUserPart(Consumer<Object> consumer) {
//    }
//
//    default void onSubscribe(Consumer<Object> consumer) {
//    }
}
