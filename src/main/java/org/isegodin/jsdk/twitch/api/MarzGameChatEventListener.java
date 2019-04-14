package org.isegodin.jsdk.twitch.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.isegodin.jsdk.twitch.api.service.chat.ChatApi;
import org.isegodin.jsdk.twitch.api.service.chat.ChatEventListener;
import org.isegodin.jsdk.twitch.api.service.chat.event.TextMessage;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author isegodin
 */
@Log4j2
@Data
@AllArgsConstructor
public class MarzGameChatEventListener implements ChatEventListener {

    private static final List<String> voteItemsWeight = Arrays.asList(
            "ЗДОРОВЯК",
            "БЕГУН",
            "ХОДОК",
            "СЛАБАК",
            "ПОЛЗУН"
    );

    private static final List<String> typeItemsWeight = Arrays.asList(
            "FREE NUKE",
            "BUILDING MATERIALS",
            "EXTRA CREW",
            "NOTHING"
    );

    private String gameBotUsername;

    @Override
    public void onTextMessageReceive(TextMessage textMessage, ChatApi chatApi) {
        if (!gameBotUsername.equals(textMessage.getUsername())) {
            return;
        }

        if (textMessage.getMessage().startsWith("vote")) {
            List<Option> options = parseOptions(textMessage.getMessage());
            sortByWeight(options, voteItemsWeight);
            chatApi.sendMessage(options.get(0).getName());
        } else if (textMessage.getMessage().startsWith("type:")) {
            List<Option> options = parseOptions(textMessage.getMessage());
            if (options.size() > 1) {
                sortByWeight(options, typeItemsWeight);
            }
            chatApi.sendMessage(options.get(0).getName());
        } else {
            log.info("Unknown message: {}", textMessage);
        }
    }

    private List<Option> parseOptions(String message) {
        List<Option> options = new LinkedList<>();

        int startIdx;
        int nextIdx = 0;

        while ((startIdx = message.indexOf("#", nextIdx)) != -1) {
            int delimeterIdx = message.indexOf(" ", startIdx);
            String name = message.substring(startIdx, delimeterIdx);
            String description;

            nextIdx = message.indexOf("#", startIdx+1);

            if (nextIdx == -1) {
                description = message.substring(delimeterIdx + 1);
            } else {
                description = message.substring(delimeterIdx + 1, nextIdx);
            }

            nextIdx = delimeterIdx + description.length() + 1;

            options.add(Option.builder()
                    .name(name)
                    .description(description.trim())
                    .build());
        }

        return options;
    }

    private void sortByWeight(List<Option> options, List<String> weights) {
        options.sort((o1, o2) -> {
            Integer w1 = findWeight(o1, weights);
            Integer w2 = findWeight(o2, weights);

            return w1.compareTo(w2);
        });
    }

    private Integer findWeight(Option option, List<String> weights) {
        for (int i = 0; i < weights.size(); i++) {
            if (option.getDescription().contains(weights.get(i))) {
               return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    @Data
    @Builder
    private static class Option {
        String name;
        String description;
    }
}
