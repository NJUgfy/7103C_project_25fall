package com.datamining.ssedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 盘口档位信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookLevel {
    private double price;
    private double volume;
}
