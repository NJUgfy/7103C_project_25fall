package com.datamining.ssedemo.service;

import com.datamining.ssedemo.dto.ChatReq;
import com.datamining.ssedemo.dto.CombinedContext;
import com.datamining.ssedemo.dto.ExtractResult;
import com.datamining.ssedemo.dto.MarketSnapshot;
import com.datamining.ssedemo.dto.NewsItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 串联 LLM 与外部服务的主流程服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWorkflowService {

    private final LLMService llmService;
    private final ExternalServiceCoordinator externalServiceCoordinator;
    private final ObjectMapper objectMapper;

    @Value("${workflow.lookback-hours:24}")
    private long lookbackHours;

    /**
     * 执行完整链路：意图抽取 -> 外部服务查询 -> LLM 生成建议 -> SSE 事件。
     */
    public Flux<String> process(ChatReq req) {
        return Flux.create(sink -> {
            AtomicBoolean finished = new AtomicBoolean(false);
            try {
                String userText = getUserText(req);
                ExtractResult extract = llmService.extract(userText);
                sink.next(event("extract", extract));

                Duration lookback = Duration.ofHours(lookbackHours);
                List<NewsItem> news = externalServiceCoordinator.fetchNews(extract, lookback);
                sink.next(event("news", news));

                List<MarketSnapshot> markets = externalServiceCoordinator.fetchMarket(extract, lookback);
                sink.next(event("market", markets));

                CombinedContext ctx = buildContext(req, userText, extract, news, markets);
                String finalAdvice = llmService.finalAdvice(ctx);
                sink.next(event("final", finalAdvice));
                completeOnce(sink, finished);
            } catch (Exception ex) {
                log.error("工作流处理失败", ex);
                sink.next(event("error", Map.of("message", Objects.toString(ex.getMessage(), "unknown error"))));
                completeOnce(sink, finished);
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    private CombinedContext buildContext(ChatReq req,
                                         String userText,
                                         ExtractResult extract,
                                         List<NewsItem> news,
                                         List<MarketSnapshot> markets) {
        CombinedContext ctx = new CombinedContext();
        ctx.setUserId(req.getChatId());
        ctx.setUserText(userText);
        ctx.setExtract(extract);
        ctx.setNews(news);
        ctx.setMarkets(markets);
        return ctx;
    }

    private String getUserText(ChatReq req) {
        if (req.getContent() != null && !req.getContent().isBlank()) {
            return req.getContent();
        }
        return req.getMessage();
    }

    private void completeOnce(FluxSink<String> sink, AtomicBoolean flag) {
        if (flag.compareAndSet(false, true)) {
            sink.next(event("done", null));
            sink.next("[DONE]");
            sink.complete();
        }
    }

    private String event(String type, Object data) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", type);
        if (data != null) {
            root.set("data", objectMapper.valueToTree(data));
        }
        return root.toString();
    }
}
