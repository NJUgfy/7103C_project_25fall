package com.datamining.ssedemo.dto;

import lombok.Data;

import java.util.List;

@Data
public class GetChatIdsResp {
    private List<String> chatIds;
}
