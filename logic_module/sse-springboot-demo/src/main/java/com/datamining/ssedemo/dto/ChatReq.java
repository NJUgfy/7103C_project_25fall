package com.datamining.ssedemo.dto;

import lombok.Data;

@Data
public class ChatReq {
    String content;
    String message;
    String chatId;
}
