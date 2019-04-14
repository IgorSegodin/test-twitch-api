package org.isegodin.jsdk.twitch.api.service.chat.event;

import lombok.Builder;
import lombok.Data;

/**
 * @author isegodin
 */
@Data
@Builder
public class TextMessage {

    private String username;
    private String message;
}
