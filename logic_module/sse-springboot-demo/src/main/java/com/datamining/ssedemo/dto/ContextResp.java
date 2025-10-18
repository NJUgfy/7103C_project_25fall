package com.datamining.ssedemo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ContextResp {
    private String chatId;
    private List<MessageVO> messages;
}
