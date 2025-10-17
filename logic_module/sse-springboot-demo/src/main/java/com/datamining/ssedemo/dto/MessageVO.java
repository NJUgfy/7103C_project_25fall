package com.datamining.ssedemo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

/**
 * context
 */
@Data
@NoArgsConstructor
public class MessageVO {

    /**
     * 角色 user/assistant
     */
    private String role;

    /**
     * 内容
     */
    private String content;


    public MessageVO(Message message) {
        switch (message.getMessageType()){
            case USER -> role = "user";
            case ASSISTANT ->  role = "assistant";
            default -> role = "unknown";
        }
        this.content = message.getText();
    }
}
