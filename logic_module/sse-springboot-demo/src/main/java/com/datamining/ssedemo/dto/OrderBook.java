package com.datamining.ssedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 盘口深度信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBook {
    private List<OrderBookLevel> bids;
    private List<OrderBookLevel> asks;
}
