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
            请从以下用户输入中抽取财经意图，输出严格的 JSON（只输出 JSON，不要额外文字），并与用户语言保持一致：用户用中文就输出中文，用户用英文就输出英文。

            字段：
            - language: "zh" | "en"
            - focus: "market" | "news" | "both"
            - products: [交易代码，仅在确定时填写；加密请用 OKX 常见格式如 BTC-USDT、ETH-USDT，避免指数/泛词/不支持的 symbol]
            - market_keywords: [行情检索关键词/符号]
            - news_keywords: [新闻检索关键词，必须是具体的主题/事件/机构/标的，不要时间词如“recent news/past day/过去一天”]
            - news_categories: [新闻类别，如 policy, macroeconomics, markets, technology, stocks]
            - reasoning: "简要说明推理"

            用户输入：
            %s
            """;

    private static final String ADVICE_SYSTEM_PROMPT_ZH = "You are a professional Chinese financial advisor. "
            + "Write concise, actionable advice with clear risk reminders. Reply in Chinese.";

    private static final String ADVICE_SYSTEM_PROMPT_EN = "You are a professional financial advisor. "
            + "Reply in English with concise, actionable advice and clear risk warnings.";

    private static final String ADVICE_USER_TEMPLATE_ZH = """
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

    private static final String ADVICE_USER_TEMPLATE_EN = """
            User question: %s

            Parsed intent:
            - News categories: %s
            - Focus tickers: %s

            External news:
            %s

            Market data:
            %s

            Please provide structured investment advice that includes at least:
            1) Market view
            2) Recommended actions or watchpoints
            3) Risk warnings
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
            List<String> categories = readStringArray(root.get("news_categories"));
            if (categories.isEmpty()) {
                categories = readStringArray(root.get("newsCategories"));
            }
            List<String> products = readStringArray(root.get("products"));
            List<String> marketKeywords = readStringArray(root.get("market_keywords"));
            if (marketKeywords.isEmpty()) {
                marketKeywords = readStringArray(root.get("marketKeywords"));
            }
            List<String> newsKeywords = readStringArray(root.get("news_keywords"));
            if (newsKeywords.isEmpty()) {
                newsKeywords = readStringArray(root.get("newsKeywords"));
            }
            String language = root.path("language").asText("unknown");
            String focus = root.path("focus").asText("both");
            String reasoning = root.path("reasoning").asText("");
            ExtractResult result = new ExtractResult(categories, products, reasoning);
            result.setMarketKeywords(marketKeywords);
            result.setNewsKeywords(newsKeywords);
            result.setLanguage(language);
            result.setFocus(focus);
            return result;
        } catch (Exception ex) {
            log.warn("Failed to extract intent, fallback to empty result", ex);
            ExtractResult result = new ExtractResult(Collections.emptyList(), Collections.emptyList(), "fallback");
            result.setMarketKeywords(Collections.emptyList());
            result.setNewsKeywords(Collections.emptyList());
            result.setLanguage("unknown");
            result.setFocus("both");
            return result;
        }
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public void streamAdvice(CombinedContext ctx, Consumer<String> onChunk, Runnable onDone, String chatId) {
        AtomicBoolean completed = new AtomicBoolean(false);
        String language = detectLanguage(ctx);
        chatClient.prompt()
                .system(selectAdviceSystemPrompt(language))
                .user(buildAdvicePrompt(ctx, language))
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
            String language = detectLanguage(ctx);
            return chatClient.prompt()
                    .system(selectAdviceSystemPrompt(language) + "\n\n" + buildAdvicePrompt(ctx, language))
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

    private String buildAdvicePrompt(CombinedContext ctx, String language) {
        ExtractResult extract = ctx.getExtract();
        String categories = extract != null && extract.getNewsCategories() != null && !extract.getNewsCategories().isEmpty()
                ? String.join(", ", extract.getNewsCategories())
                : "未知";
        String products = extract != null && extract.getProducts() != null && !extract.getProducts().isEmpty()
                ? String.join(", ", extract.getProducts())
                : "未识别";
        String newsBlock = renderNews(ctx.getNews(), language);
        String marketBlock = renderMarkets(ctx.getMarkets(), language);
        String userText = Objects.toString(ctx.getUserText(), "");
        if ("en".equalsIgnoreCase(language)) {
            categories = categories.equals("未知") ? "unknown" : categories;
            products = products.equals("未识别") ? "unknown" : products;
            return String.format(ADVICE_USER_TEMPLATE_EN, userText, categories, products, newsBlock, marketBlock);
        }
        return String.format(ADVICE_USER_TEMPLATE_ZH, userText, categories, products, newsBlock, marketBlock);
    }

    private String selectAdviceSystemPrompt(String language) {
        return "en".equalsIgnoreCase(language) ? ADVICE_SYSTEM_PROMPT_EN : ADVICE_SYSTEM_PROMPT_ZH;
    }

    private String detectLanguage(CombinedContext ctx) {
        ExtractResult extract = ctx.getExtract();
        if (extract != null && extract.getLanguage() != null) {
            String lang = extract.getLanguage().trim().toLowerCase();
            if (lang.equals("en") || lang.equals("zh")) {
                return lang;
            }
        }
        return "zh";
    }

    private String renderNews(List<NewsItem> items, String language) {
        if (items == null || items.isEmpty()) {
            return "en".equalsIgnoreCase(language) ? "- No related news" : "- 暂无相关新闻";
        }
        return items.stream()
                .map(item -> formatNewsItem(item, language))
                .collect(Collectors.joining("\n\n"));
    }

    private String formatNewsItem(NewsItem item, String language) {
        String published = Objects.toString(item.getPublishedAt(), "未知时间");
        String summary = Objects.toString(item.getSummary(), "暂无摘要");
        if ("en".equalsIgnoreCase(language)) {
            published = Objects.toString(item.getPublishedAt(), "unknown time");
            summary = Objects.toString(item.getSummary(), "no summary");
            return String.format("- %s (%s, %s) %s%n  Summary: %s",
                    Objects.toString(item.getTitle(), "unknown title"),
                    Objects.toString(item.getSource(), "unknown source"),
                    published,
                    Objects.toString(item.getUrl(), "#"),
                    summary);
        }
        return String.format("- %s (%s, %s) %s%n  摘要: %s",
                Objects.toString(item.getTitle(), "未知标题"),
                Objects.toString(item.getSource(), "未知来源"),
                published,
                Objects.toString(item.getUrl(), "#"),
                summary);
    }

    private String renderMarkets(List<MarketSnapshot> snapshots, String language) {
        if (snapshots == null || snapshots.isEmpty()) {
            return "en".equalsIgnoreCase(language) ? "- No market data" : "- 未获取到行情数据";
        }
        StringBuilder builder = new StringBuilder();
        for (MarketSnapshot snapshot : snapshots) {
            if ("en".equalsIgnoreCase(language)) {
                builder.append(String.format("- %s Last: %.2f (24h change: %.2f%%) High: %.2f Low: %.2f Vol: %.2f RSI: %.2f%n",
                        snapshot.getSymbol(),
                        snapshot.getLastPrice(),
                        snapshot.getChangePercent(),
                        snapshot.getHigh24h(),
                        snapshot.getLow24h(),
                        snapshot.getVolume24h(),
                        snapshot.getRsi()));
            } else {
                builder.append(String.format("- %s 最新价: %.2f (24h涨跌: %.2f%%) 高: %.2f 低: %.2f 量: %.2f RSI: %.2f%n",
                        snapshot.getSymbol(),
                        snapshot.getLastPrice(),
                        snapshot.getChangePercent(),
                        snapshot.getHigh24h(),
                        snapshot.getLow24h(),
                        snapshot.getVolume24h(),
                        snapshot.getRsi()));
            }
            OrderBook orderBook = snapshot.getOrderBook();
            if (orderBook != null) {
                if ("en".equalsIgnoreCase(language)) {
                    builder.append("  Bids: ").append(formatLevels(orderBook.getBids())).append("\n");
                    builder.append("  Asks: ").append(formatLevels(orderBook.getAsks())).append("\n");
                } else {
                    builder.append("  买单: ").append(formatLevels(orderBook.getBids())).append("\n");
                    builder.append("  卖单: ").append(formatLevels(orderBook.getAsks())).append("\n");
                }
            }
            if (snapshot.getKlines() != null && !snapshot.getKlines().isEmpty()) {
                if ("en".equalsIgnoreCase(language)) {
                    builder.append("  Last 3 klines: ").append(formatKlines(snapshot.getKlines(), true)).append("\n");
                } else {
                    builder.append("  近三条K线: ").append(formatKlines(snapshot.getKlines(), false)).append("\n");
                }
            }
        }
        String result = builder.toString().trim();
        if (result.isEmpty()) {
            return "en".equalsIgnoreCase(language) ? "- No market data" : "- 未获取到行情数据";
        }
        return result;
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

    private String formatKlines(List<KlinePoint> klines, boolean en) {
        int size = klines.size();
        int fromIndex = Math.max(0, size - 3);
        List<KlinePoint> tail = klines.subList(fromIndex, size);
        return tail.stream()
                .map(k -> en
                        ? String.format("%s close %.2f vol %.2f", formatTime(k.getStartTime()), k.getClose(), k.getVolume())
                        : String.format("%s 收盘%.2f 量%.2f", formatTime(k.getStartTime()), k.getClose(), k.getVolume()))
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
