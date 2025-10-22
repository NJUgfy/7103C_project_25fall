package com.datamining.ssedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * K线数据点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KlinePoint {
    private long startTime;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
}
