package com.datamining.ssedemo.service;

import com.datamining.ssedemo.dto.CombinedContext;
import com.datamining.ssedemo.dto.ExtractResult;
import java.util.function.Consumer;

public interface LLMService {
    ExtractResult extract(String userText, String chatId);
    boolean supportsStreaming();
    void streamAdvice(CombinedContext ctx, Consumer<String> onChunk, Runnable onDone, String chatId);
    String finalAdvice(CombinedContext ctx,String chatId);
}
