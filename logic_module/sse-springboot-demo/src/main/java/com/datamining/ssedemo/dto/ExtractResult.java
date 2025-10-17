package com.datamining.ssedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractResult {
    private List<String> newsCategories;
    private List<String> products;
    private String reasoning;
}
