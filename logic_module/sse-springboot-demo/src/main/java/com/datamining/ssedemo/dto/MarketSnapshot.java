package com.datamining.ssedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketSnapshot {
    private String symbol;
    private double high;
    private double low;
    private double volume;
    private double rsi;
}
