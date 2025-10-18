package com.datamining.ssedemo.dto;

import lombok.Data;

import java.util.List;
@Data
public class CombinedContext {
    private String userId;
    private String userText;
    private ExtractResult extract;
    private List<NewsItem> news;
    private List<MarketSnapshot> markets;
}
