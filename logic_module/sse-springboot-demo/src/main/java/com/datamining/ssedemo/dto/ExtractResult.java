package com.datamining.ssedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
public class ExtractResult {
    private List<String> newsCategories;
    private List<String> products;
    /**
     * 语言识别: zh / en / unknown
     */
    private String language;
    /**
     * 关注重点: market / news / both
     */
    private String focus;
    /**
     * 行情检索关键词，兜底 products 为空的情况
     */
    private List<String> marketKeywords;
    /**
     * 新闻检索关键词，优先用于 news 搜索
     */
    private List<String> newsKeywords;
    private String reasoning;

    /**
     * 保持对旧代码的兼容的三参构造。
     */
    public ExtractResult(List<String> newsCategories, List<String> products, String reasoning) {
        this.newsCategories = newsCategories;
        this.products = products;
        this.reasoning = reasoning;
    }
}
