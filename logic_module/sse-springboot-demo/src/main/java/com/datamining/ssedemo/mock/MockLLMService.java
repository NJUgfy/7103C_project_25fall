package com.datamining.ssedemo.mock;

import com.datamining.ssedemo.dto.CombinedContext;
import com.datamining.ssedemo.dto.ExtractResult;
import com.datamining.ssedemo.service.LLMService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@ConditionalOnProperty(name = "demo.mock", havingValue = "true", matchIfMissing = true)
public class MockLLMService implements LLMService {
    @Override
    public ExtractResult extract(String userText) {
        // 简单“抽取”：关键词命中
        List<String> cats = userText.toLowerCase().contains("军") ? List.of("military") : List.of("finance");
        List<String> prods = userText.toLowerCase().contains("btc") ? List.of("BTCUSDT") : List.of("AAPL");
        return new ExtractResult(cats, prods, "mock reasoning");
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public void streamAdvice(CombinedContext ctx, Consumer<String> onChunk, Runnable onDone) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        for (int i = 1; i <= 5; i++) {
            int k = i;
            scheduler.schedule(() -> onChunk.accept("{\"chunk\":\"part-\" + k + \"\"}"), i * 400L, TimeUnit.MILLISECONDS);
        }
        scheduler.schedule(onDone, 2600L, TimeUnit.MILLISECONDS);
        scheduler.shutdown();
    }

    @Override
    public String finalAdvice(CombinedContext ctx) {
        return "{\"advice\":\"buy&hold (mock)\"}";
    }
}
