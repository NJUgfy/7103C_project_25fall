package com.datamining.ssedemo.service;

import com.datamining.ssedemo.dto.ExtractResult;
import com.datamining.ssedemo.dto.MarketSnapshot;
import com.datamining.ssedemo.dto.NewsItem;

import java.time.Duration;
import java.util.List;

public interface ExternalServiceCoordinator {
    List<NewsItem> fetchNews(ExtractResult extract, Duration lookback);
    List<MarketSnapshot> fetchMarket(ExtractResult extract, Duration lookback);
}
