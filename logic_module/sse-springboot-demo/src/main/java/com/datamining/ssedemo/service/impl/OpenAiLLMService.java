package com.datamining.ssedemo.service.impl;

import com.datamining.ssedemo.dto.CombinedContext;
import com.datamining.ssedemo.dto.ExtractResult;
import com.datamining.ssedemo.dto.KlinePoint;
import com.datamining.ssedemo.dto.MarketSnapshot;
import com.datamining.ssedemo.dto.NewsItem;
import com.datamining.ssedemo.dto.OrderBook;
import com.datamining.ssedemo.dto.OrderBookLevel;
import com.datamining.ssedemo.service.LLMService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * 使用 Spring AI OpenAI 客户端的真实 LLMService 实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "demo.mock", havingValue = "false")
public class OpenAiLLMService implements LLMService {

    private static final String EXTRACT_SYSTEM_PROMPT = "You are an assistant that identifies financial intents, instruments and news categories. "
            // + "Always answer with strict JSON only."
            ;

    private static final String EXTRACT_USER_TEMPLATE = """
            请从以下用户输入中抽取财经资讯类别与涉及的金融产品，并说明推理：
            输出格式固定为 JSON，包含字段：
            - newsCategories: 字符串数组（英文小写主题，例如 finance、policy）
            - products: 字符串数组（使用常见交易代码，例如 BTC-USDT、AAPL）
            - reasoning: 字符串，描述你的推理过程

            用户输入：
            %s
            """;

    private static final String ADVICE_SYSTEM_PROMPT = "You are a professional Chinese financial advisor. "
            + "Write concise, actionable advice with clear risk reminders.";

    private static final String ADVICE_USER_TEMPLATE = """
            用户原始问题: %s

            已解析意图:
            - 资讯类别: %s
            - 关注标的: %s

            外部新闻:
            %s

            行情数据:
            %s

            请结合以上信息，输出自然语言的结构化投资建议，至少包含：
            1. 市场判断
            2. 建议操作或关注点
            3. 风险提示
            """;

    private static final DateTimeFormatter KLINE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneOffset.UTC);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public ExtractResult extract(String userText, String chatId) {
        try {
            String raw = chatClient.prompt()
                    .system(EXTRACT_SYSTEM_PROMPT)
                    .user(String.format(EXTRACT_USER_TEMPLATE, userText))
                    .call()
                    .content();
            JsonNode root = objectMapper.readTree(raw);
            List<String> categories = readStringArray(root.get("newsCategories"));
            List<String> products = readStringArray(root.get("products"));
            String reasoning = root.path("reasoning").asText("");
            return new ExtractResult(categories, products, reasoning);
        } catch (Exception ex) {
            log.warn("Failed to extract intent, fallback to empty result", ex);
            return new ExtractResult(Collections.emptyList(), Collections.emptyList(), "fallback");
        }
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public void streamAdvice(CombinedContext ctx, Consumer<String> onChunk, Runnable onDone, String chatId) {
        AtomicBoolean completed = new AtomicBoolean(false);
        chatClient.prompt()
                .system(ADVICE_SYSTEM_PROMPT)
                .user(buildAdvicePrompt(ctx))
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .stream()
                .content()
                .subscribe(
                        chunk -> safeAccept(onChunk, chunk),
                        ex -> {
                            log.error("LLM streaming failed", ex);
                            runOnce(onDone, completed);
                        },
                        () -> runOnce(onDone, completed)
                );
    }

    @Override
    public String finalAdvice(CombinedContext ctx, String chatId) {
        try {
            return chatClient.prompt()
                    .system(ADVICE_SYSTEM_PROMPT+ "\n\n" + buildAdvicePrompt(ctx))
                    .user(ctx.getUserText())
                    .advisors(a -> a.param(CONVERSATION_ID, chatId))
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("LLM final advice failed", ex);
            return "{\"error\":\"advice generation failed\"}";
        }
    }

    private List<String> readStringArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return Collections.emptyList();
        }
        return objectMapper.convertValue(node, new TypeReference<List<String>>() {
        });
    }

    private String buildAdvicePrompt(CombinedContext ctx) {
        ExtractResult extract = ctx.getExtract();
        String categories = extract != null && extract.getNewsCategories() != null && !extract.getNewsCategories().isEmpty()
                ? String.join(", ", extract.getNewsCategories())
                : "未知";
        String products = extract != null && extract.getProducts() != null && !extract.getProducts().isEmpty()
                ? String.join(", ", extract.getProducts())
                : "未识别";
        String newsBlock = renderNews(ctx.getNews());
        String marketBlock = renderMarkets(ctx.getMarkets());
        String userText = Objects.toString(ctx.getUserText(), "");
        return String.format(ADVICE_USER_TEMPLATE, userText, categories, products, newsBlock, marketBlock);
    }

    private String renderNews(List<NewsItem> items) {
        if (items == null || items.isEmpty()) {
            return "- 暂无相关新闻";
        }
        return items.stream()
                .map(this::formatNewsItem)
                .collect(Collectors.joining("\n\n"));
    }

    private String formatNewsItem(NewsItem item) {
        String published = Objects.toString(item.getPublishedAt(), "未知时间");
        String summary = Objects.toString(item.getSummary(), "暂无摘要");
        return String.format("- %s (%s, %s) %s%n  摘要: %s",
                Objects.toString(item.getTitle(), "未知标题"),
                Objects.toString(item.getSource(), "未知来源"),
                published,
                Objects.toString(item.getUrl(), "#"),
                summary);
    }

    private String renderMarkets(List<MarketSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return "- 未获取到行情数据";
        }
        StringBuilder builder = new StringBuilder();
        for (MarketSnapshot snapshot : snapshots) {
            builder.append(String.format("- %s 最新价: %.2f (24h涨跌: %.2f%%) 高: %.2f 低: %.2f 量: %.2f RSI: %.2f%n",
                    snapshot.getSymbol(),
                    snapshot.getLastPrice(),
                    snapshot.getChangePercent(),
                    snapshot.getHigh24h(),
                    snapshot.getLow24h(),
                    snapshot.getVolume24h(),
                    snapshot.getRsi()));
            OrderBook orderBook = snapshot.getOrderBook();
            if (orderBook != null) {
                builder.append("  买单: ").append(formatLevels(orderBook.getBids())).append("\n");
                builder.append("  卖单: ").append(formatLevels(orderBook.getAsks())).append("\n");
            }
            if (snapshot.getKlines() != null && !snapshot.getKlines().isEmpty()) {
                builder.append("  近三条K线: ").append(formatKlines(snapshot.getKlines())).append("\n");
            }
        }
        String result = builder.toString().trim();
        return result.isEmpty() ? "- 未获取到行情数据" : result;
    }

    private String formatLevels(List<OrderBookLevel> levels) {
        if (levels == null || levels.isEmpty()) {
            return "无";
        }
        return levels.stream()
                .limit(3)
                .map(level -> String.format("%.2f@%.2f", level.getPrice(), level.getVolume()))
                .collect(Collectors.joining(", "));
    }

    private String formatKlines(List<KlinePoint> klines) {
        int size = klines.size();
        int fromIndex = Math.max(0, size - 3);
        List<KlinePoint> tail = klines.subList(fromIndex, size);
        return tail.stream()
                .map(k -> String.format("%s 收盘%.2f 量%.2f",
                        formatTime(k.getStartTime()), k.getClose(), k.getVolume()))
                .collect(Collectors.joining(" | "));
    }

    private String formatTime(long epochMillis) {
        if (epochMillis <= 0) {
            return "-";
        }
        try {
            return KLINE_TIME_FORMATTER.format(Instant.ofEpochMilli(epochMillis));
        } catch (Exception ex) {
            return "-";
        }
    }

    private void safeAccept(Consumer<String> consumer, String chunk) {
        if (consumer == null) {
            return;
        }
        try {
            consumer.accept(chunk);
        } catch (Exception ex) {
            log.warn("Chunk consumer threw exception", ex);
        }
    }

    private void runOnce(Runnable runnable, AtomicBoolean flag) {
        if (runnable != null && flag.compareAndSet(false, true)) {
            try {
                runnable.run();
            } catch (Exception ex) {
                log.warn("Completion callback threw exception", ex);
            }
        }
    }
}
