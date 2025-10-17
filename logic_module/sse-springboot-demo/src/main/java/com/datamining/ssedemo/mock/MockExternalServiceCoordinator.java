package com.datamining.ssedemo.mock;

import com.datamining.ssedemo.dto.ExtractResult;
import com.datamining.ssedemo.dto.MarketSnapshot;
import com.datamining.ssedemo.dto.NewsItem;
import com.datamining.ssedemo.service.ExternalServiceCoordinator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@ConditionalOnProperty(name = "demo.mock", havingValue = "true", matchIfMissing = true)
public class MockExternalServiceCoordinator implements ExternalServiceCoordinator {
    @Override
    public List<NewsItem> fetchNews(ExtractResult extract, Duration lookback) {
        return List.of(
                new NewsItem("Headline 1", "MockWire", "https://example.com/1"),
                new NewsItem("Headline 2", "MockWire", "https://example.com/2")
        );
    }

    @Override
    public List<MarketSnapshot> fetchMarket(ExtractResult extract, Duration lookback) {
        return extract.getProducts().stream()
                .map(sym -> new MarketSnapshot(sym, 100.0, 90.0, 1234567, 48.2))
                .toList();
    }
}
