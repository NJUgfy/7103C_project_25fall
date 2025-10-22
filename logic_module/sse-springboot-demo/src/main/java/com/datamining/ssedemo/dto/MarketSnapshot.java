package com.datamining.ssedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketSnapshot {
    private String symbol;
    private double lastPrice;
    private double high24h;
    private double low24h;
    private double volume24h;
    private double changePercent;
    private double rsi;
    private List<KlinePoint> klines;
    private OrderBook orderBook;
}
