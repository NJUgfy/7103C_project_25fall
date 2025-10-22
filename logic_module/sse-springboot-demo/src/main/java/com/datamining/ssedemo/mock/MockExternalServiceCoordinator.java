package com.datamining.ssedemo.mock;

import com.datamining.ssedemo.dto.ExtractResult;
import com.datamining.ssedemo.dto.KlinePoint;
import com.datamining.ssedemo.dto.MarketSnapshot;
import com.datamining.ssedemo.dto.NewsItem;
import com.datamining.ssedemo.dto.OrderBook;
import com.datamining.ssedemo.dto.OrderBookLevel;
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
                new NewsItem("Headline 1", "MockWire", "https://example.com/1",
                        "Mock summary for headline 1", "2025-10-22T08:30:00Z"),
                new NewsItem("Headline 2", "MockWire", "https://example.com/2",
                        "Mock summary for headline 2", "2025-10-22T09:15:00Z")
        );
    }

    @Override
    public List<MarketSnapshot> fetchMarket(ExtractResult extract, Duration lookback) {
        List<String> products = extract == null || extract.getProducts() == null || extract.getProducts().isEmpty()
                ? List.of("MOCK_SYMBOL")
                : extract.getProducts();
        return products.stream()
                .map(sym -> {
                    List<KlinePoint> klines = List.of(
                            new KlinePoint(System.currentTimeMillis() - 3600_000L, 100.0, 102.0, 99.0, 101.5, 1200),
                            new KlinePoint(System.currentTimeMillis() - 1800_000L, 101.5, 103.0, 100.5, 102.2, 980),
                            new KlinePoint(System.currentTimeMillis() - 600_000L, 102.2, 104.0, 101.0, 103.8, 1500)
                    );
                    OrderBook orderBook = new OrderBook(
                            List.of(new OrderBookLevel(103.5, 12), new OrderBookLevel(103.0, 18), new OrderBookLevel(102.5, 25)),
                            List.of(new OrderBookLevel(104.2, 10), new OrderBookLevel(104.8, 16), new OrderBookLevel(105.3, 20))
                    );
                    return new MarketSnapshot(
                            sym,
                            103.8,
                            110.0,
                            95.0,
                            456789,
                            1.25,
                            55.6,
                            klines,
                            orderBook
                    );
                })
                .toList();
    }
}
