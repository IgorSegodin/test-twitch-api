package org.isegodin.jsdk.twitch.api;

import org.isegodin.jsdk.twitch.api.service.chat.ChatApi;
import org.isegodin.jsdk.twitch.api.service.chat.event.TextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

/**
 * @author isegodin
 */
@RunWith(MockitoJUnitRunner.class)
public class MarzGameChatEventListenerTest {

    private MarzGameChatEventListener eventListener = new MarzGameChatEventListener("gameBot");

    @Mock
    private ChatApi chatApi;
    //vote for next wave: #1 for БЕГУН #2 for ХОДОК #3 for ЗДОРОВЯК
    //vote for next wave: #1 for ХОДОК #2 for СЛАБАК #3 for ЗДОРОВЯК

    //#3 for ПОЛЗУН

    //type: #produce to speed up superweapon production
    //type: #a for +500 BUILDING MATERIALS // #b for EXTRA CREW // #c for +1 FREE NUKE // #d for NOTHING :P

    @Test
    public void testOnVoteTextMessageReceive() {
        eventListener.onTextMessageReceive(TextMessage.builder()
                .username("gameBot")
                .message("vote for next wave: #1 for БЕГУН #2 for ХОДОК #3 for ЗДОРОВЯК")
                .build(), chatApi);

        ArgumentCaptor<String> messageArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatApi, Mockito.atLeastOnce())
                .sendMessage(messageArgumentCaptor.capture());

        assertEquals("#3", messageArgumentCaptor.getValue());
    }
    @Test
    public void testOnTypeTextMessageReceive() {
        eventListener.onTextMessageReceive(TextMessage.builder()
                .username("gameBot")
                .message("type: #a for +500 BUILDING MATERIALS // #b for EXTRA CREW // #c for +1 FREE NUKE // #d for NOTHING")
                .build(), chatApi);

        ArgumentCaptor<String> messageArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatApi, Mockito.atLeastOnce())
                .sendMessage(messageArgumentCaptor.capture());

        assertEquals("#c", messageArgumentCaptor.getValue());
    }
}