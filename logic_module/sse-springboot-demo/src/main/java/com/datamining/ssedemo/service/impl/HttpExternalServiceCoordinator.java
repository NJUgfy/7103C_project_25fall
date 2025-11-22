package com.datamining.ssedemo.service.impl;

import com.datamining.ssedemo.dto.ExtractResult;
import com.datamining.ssedemo.dto.KlinePoint;
import com.datamining.ssedemo.dto.MarketSnapshot;
import com.datamining.ssedemo.dto.NewsItem;
import com.datamining.ssedemo.dto.OrderBook;
import com.datamining.ssedemo.dto.OrderBookLevel;
import com.datamining.ssedemo.service.ExternalServiceCoordinator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通过 HTTP 调用新闻与行情服务的协调器实现。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "demo.mock", havingValue = "false")
public class HttpExternalServiceCoordinator implements ExternalServiceCoordinator {

    private static final Duration DEFAULT_LOOKBACK = Duration.ofHours(24);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int RSI_PERIOD = 14;

    private final RestTemplate newsRestTemplate;
    private final RestTemplate marketRestTemplate;
    private final ObjectMapper objectMapper;
    private final String newsBaseUrl;
    private final String marketBaseUrl;
    private volatile boolean newsServiceEnabled = true;
    private volatile boolean marketServiceEnabled = true;

    public HttpExternalServiceCoordinator(RestTemplateBuilder builder,
                                          ObjectMapper objectMapper,
                                          @Value("${external.services.news.base-url:http://127.0.0.1:9106/irls/news}") String newsBaseUrl,
                                          @Value("${external.services.news.timeout:PT5S}") Duration newsTimeout,
                                          @Value("${external.services.news.enabled:true}") boolean newsEnabled,
                                          @Value("${external.services.market.base-url:http://127.0.0.1:9105/irls/market}") String marketBaseUrl,
                                          @Value("${external.services.market.timeout:PT5S}") Duration marketTimeout,
                                          @Value("${external.services.market.enabled:true}") boolean marketEnabled) {
        this.objectMapper = objectMapper;
        this.newsRestTemplate = builder
                .rootUri(newsBaseUrl)
                .setConnectTimeout(newsTimeout)
                .setReadTimeout(newsTimeout)
                .build();
        this.marketRestTemplate = builder
                .rootUri(marketBaseUrl)
                .setConnectTimeout(marketTimeout)
                .setReadTimeout(marketTimeout)
                .build();
        this.newsServiceEnabled = newsEnabled;
        this.marketServiceEnabled = marketEnabled;
        this.newsBaseUrl = newsBaseUrl;
        this.marketBaseUrl = marketBaseUrl;
    }

    @Override
    public List<NewsItem> fetchNews(ExtractResult extract, Duration lookback) {
        if (!newsServiceEnabled) {
            return Collections.emptyList();
        }
        Duration window = normalizeWindow(lookback);
        Instant now = Instant.now();
        Instant from = now.minus(window);
        String keyword = buildKeyword(extract);
        String region = "en";
        if (extract != null && "zh".equalsIgnoreCase(extract.getLanguage())) {
            region = "zh";
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(newsBaseUrl)
                .path("/search")
                .queryParam("keyword", keyword)
                .queryParam("from", DATE_FORMATTER.format(from.atZone(ZoneOffset.UTC)))
                .queryParam("to", DATE_FORMATTER.format(now.atZone(ZoneOffset.UTC)))
                .queryParam("limit", 5)
                .queryParam("sources", "")
                .queryParam("region", region)
                .build()
                .encode()
                .toUri();

        try {
            ResponseEntity<String> response = newsRestTemplate.getForEntity(uri, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("新闻服务返回异常状态: {}", response.getStatusCode());
                return Collections.emptyList();
            }
            ApiResponse<List<NewsPayload>> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            if (body.getCode() != 200 || CollectionUtils.isEmpty(body.getData())) {
                log.info("新闻服务无数据: {}", body.getMessage());
                return Collections.emptyList();
            }
            return body.getData().stream()
                    .map(item -> new NewsItem(
                            item.getTitle(),
                            item.getSource(),
                            item.getUrl(),
                            item.getSummary(),
                            item.getPublishedAt()))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("调用新闻服务失败", ex);
            newsServiceEnabled = false;
            return Collections.emptyList();
        }
    }

    @Override
    public List<MarketSnapshot> fetchMarket(ExtractResult extract, Duration lookback) {
        List<String> targets = Collections.emptyList();
        if (extract != null && !CollectionUtils.isEmpty(extract.getProducts())) {
            targets = extract.getProducts();
        } else if (extract != null && !CollectionUtils.isEmpty(extract.getMarketKeywords())) {
            targets = extract.getMarketKeywords();
        }
        targets = filterTargets(targets);
        if (CollectionUtils.isEmpty(targets)) {
            return Collections.emptyList();
        }
        if (!marketServiceEnabled) {
            return Collections.emptyList();
        }
        Duration window = normalizeWindow(lookback);
        Instant now = Instant.now();
        Instant from = now.minus(window);
        List<MarketSnapshot> snapshots = new ArrayList<>();
        for (String product : targets) {
            if (!marketServiceEnabled) {
                break;
            }
            Optional<TickerPayload> tickerOpt = fetchTicker(product);
            if (tickerOpt.isEmpty()) {
                continue;
            }
            KlineResult klineResult = fetchKlines(product, from, now);
            TickerPayload ticker = tickerOpt.get();
            OrderBook orderBook = fetchDepth(product)
                    .map(depth -> new OrderBook(
                            convertDepth(depth.getBids()),
                            convertDepth(depth.getAsks())
                    ))
                    .orElse(null);
            snapshots.add(new MarketSnapshot(
                    ticker.getSymbol(),
                    ticker.getLastPrice(),
                    ticker.getHigh24h(),
                    ticker.getLow24h(),
                    ticker.getVolume24h(),
                    ticker.getChangePercent(),
                    klineResult.getRsi(),
                    klineResult.getKlines(),
                    orderBook
            ));
        }
        return snapshots;
    }

    private Optional<TickerPayload> fetchTicker(String product) {
        URI uri = UriComponentsBuilder.fromHttpUrl(marketBaseUrl)
                .path("/get_ticker")
                .queryParam("platform", "OKX")
                .queryParam("symbol", product)
                .build()
                .encode()
                .toUri();
        try {
            ResponseEntity<String> response = marketRestTemplate.getForEntity(uri, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("行情 ticker 接口状态异常: {} for {}", response.getStatusCode(), product);
                return Optional.empty();
            }
            ApiResponse<TickerPayload> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            if (body.getCode() != 200 || body.getData() == null) {
                log.warn("行情 ticker 接口返回失败: {} - {} for {}", body.getCode(), body.getMessage(), product);
                return Optional.empty();
            }
            return Optional.of(body.getData());
        } catch (Exception ex) {
            log.error("获取 {} ticker 失败", product, ex);
            marketServiceEnabled = false;
            return Optional.empty();
        }
    }

    private KlineResult fetchKlines(String product, Instant from, Instant to) {
        URI uri = UriComponentsBuilder.fromHttpUrl(marketBaseUrl)
                .path("/get_klines")
                .queryParam("platform", "OKX")
                .queryParam("symbol", product)
                .queryParam("market_type", "SPOT")
                .queryParam("interval", "1H")
                .queryParam("start_time", from.getEpochSecond())
                .queryParam("end_time", to.getEpochSecond())
                .queryParam("limit", 120)
                .build()
                .encode()
                .toUri();
        try {
            ResponseEntity<String> response = marketRestTemplate.getForEntity(uri, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("行情 kline 接口状态异常: {}", response.getStatusCode());
                return new KlineResult(50.0, Collections.emptyList());
            }
            ApiResponse<List<KlinePayload>> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            if (body.getCode() != 200 || CollectionUtils.isEmpty(body.getData())) {
                return new KlineResult(50.0, Collections.emptyList());
            }
            List<KlinePoint> klines = body.getData().stream()
                    .map(k -> new KlinePoint(
                            Optional.ofNullable(k.getStartTime()).orElse(0L),
                            safeDouble(k.getOpenPrice()),
                            safeDouble(k.getHighPrice()),
                            safeDouble(k.getLowPrice()),
                            safeDouble(k.getClosePrice()),
                            safeDouble(k.getVolume())
                    ))
                    .collect(Collectors.toList());
            List<Double> closes = klines.stream()
                    .map(KlinePoint::getClose)
                    .collect(Collectors.toList());
            double rsi = calcRsi(closes, RSI_PERIOD);
            return new KlineResult(rsi, klines);
        } catch (Exception ex) {
            log.error("获取 {} kline 失败", product, ex);
            return new KlineResult(50.0, Collections.emptyList());
        }
    }

    private Optional<DepthPayload> fetchDepth(String product) {
        URI uri = UriComponentsBuilder.fromHttpUrl(marketBaseUrl)
                .path("/get_depth")
                .queryParam("platform", "OKX")
                .queryParam("symbol", product)
                .build()
                .encode()
                .toUri();
        try {
            ResponseEntity<String> response = marketRestTemplate.getForEntity(uri, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("行情 depth 接口状态异常: {}", response.getStatusCode());
                return Optional.empty();
            }
            ApiResponse<DepthPayload> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            if (body.getCode() != 200 || body.getData() == null) {
                log.warn("行情 depth 接口返回失败: {} - {}", body.getCode(), body.getMessage());
                return Optional.empty();
            }
            return Optional.of(body.getData());
        } catch (Exception ex) {
            log.error("获取 {} depth 失败", product, ex);
            return Optional.empty();
        }
    }

    private Duration normalizeWindow(Duration lookback) {
        if (lookback == null || lookback.isNegative() || lookback.isZero()) {
            return DEFAULT_LOOKBACK;
        }
        return lookback;
    }

    private String buildKeyword(ExtractResult extract) {
        if (extract == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        if (!CollectionUtils.isEmpty(extract.getNewsKeywords())) {
            parts.addAll(extract.getNewsKeywords());
        } else {
            if (!CollectionUtils.isEmpty(extract.getNewsCategories())) {
                parts.addAll(extract.getNewsCategories());
            }
            if (!CollectionUtils.isEmpty(extract.getProducts())) {
                parts.addAll(extract.getProducts());
            }
        }
        parts = cleanKeywords(parts);
        if (parts.isEmpty()) {
            return "";
        }
        return String.join(" OR ", parts);
    }

    /**
     * 过滤掉无效或过于泛化的关键词，避免出现“recent news”“past day”等无意义的查询词。
     */
    private List<String> cleanKeywords(List<String> parts) {
        if (CollectionUtils.isEmpty(parts)) {
            return Collections.emptyList();
        }
        Set<String> ban = Set.of(
                "recent news", "past day", "past month", "recent",
                "最近新闻", "过去一天", "过去一个月", "新闻", "最近"
        );
        return parts.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> !ban.contains(s.toLowerCase()))
                .distinct()
                .toList();
    }

    /**
     * 过滤掉明显无效的交易对，避免用“tickers”等泛词调用行情接口。
     */
    private List<String> filterTargets(List<String> raw) {
        if (CollectionUtils.isEmpty(raw)) {
            return Collections.emptyList();
        }
        return raw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(this::isSymbolLike)
                .distinct()
                .toList();
    }

    private boolean isSymbolLike(String s) {
        String v = s.trim();
        return v.matches("(?i)^[A-Z0-9]{2,10}[-_][A-Z0-9]{2,10}$");
    }

    private double calcRsi(List<Double> closes, int period) {
        if (closes.size() <= period) {
            return 50.0;
        }
        double gain = 0.0;
        double loss = 0.0;
        for (int i = 1; i <= period; i++) {
            double change = closes.get(i) - closes.get(i - 1);
            if (change > 0) {
                gain += change;
            } else {
                loss -= change;
            }
        }
        gain /= period;
        loss /= period;
        if (loss == 0) {
            return 100.0;
        }
        double rs = gain / loss;
        double rsi = 100 - (100 / (1 + rs));
        double avgGain = gain;
        double avgLoss = loss;
        for (int i = period + 1; i < closes.size(); i++) {
            double change = closes.get(i) - closes.get(i - 1);
            if (change > 0) {
                avgGain = (avgGain * (period - 1) + change) / period;
                avgLoss = (avgLoss * (period - 1)) / period;
            } else {
                avgGain = (avgGain * (period - 1)) / period;
                avgLoss = (avgLoss * (period - 1) - change) / period;
            }
            if (avgLoss == 0) {
                rsi = 100.0;
            } else {
                rs = avgGain / avgLoss;
                rsi = 100 - (100 / (1 + rs));
            }
        }
        return Math.max(0, Math.min(100, rsi));
    }

    private List<OrderBookLevel> convertDepth(List<List<Double>> levels) {
        if (CollectionUtils.isEmpty(levels)) {
            return Collections.emptyList();
        }
        return levels.stream()
                .filter(level -> level.size() >= 2)
                .limit(5)
                .map(level -> new OrderBookLevel(level.get(0), level.get(1)))
                .collect(Collectors.toList());
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiResponse<T> {
        private int code;
        private T data;
        private String message;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NewsPayload {
        private String title;
        private String source;
        private String url;
        private String summary;
        private String published_at;

        String getPublishedAt() {
            return published_at;
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TickerPayload {
        private String symbol;
        private double last_price;
        private double high24h;
        private double low24h;
        private double volume24h;
        private double change_percent;

        double getLastPrice() {
            return last_price;
        }

        double getChangePercent() {
            return change_percent;
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class KlinePayload {
        // Upstream may send number seconds/ms or a formatted string like "YYYY-MM-DD HH:MM:SS".
        private Object start_time;
        private Double open_price;
        private Double high_price;
        private Double low_price;
        private Double close_price;
        private Double volume;

        Double getClosePrice() {
            return close_price;
        }

        Long getStartTime() {
            return toEpochMillis(start_time);
        }

        Double getOpenPrice() {
            return open_price;
        }

        Double getHighPrice() {
            return high_price;
        }

        Double getLowPrice() {
            return low_price;
        }

        Double getVolume() {
            return volume;
        }

        private Long toEpochMillis(Object v) {
            try {
                if (v == null) return null;
                if (v instanceof Number num) {
                    long iv = num.longValue();
                    // If looks like seconds, convert to ms
                    return iv > 10_000_000_000L ? iv : iv * 1000L;
                }
                if (v instanceof String s) {
                    String str = s.trim();
                    // numeric string
                    if (str.matches("^-?\\d+$")) {
                        long iv = Long.parseLong(str);
                        return iv > 10_000_000_000L ? iv : iv * 1000L;
                    }
                    // try parse common pattern: 2025-11-01 01:00:00 (assume UTC)
                    try {
                        java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(str, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        return ldt.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
                    } catch (Exception ignore) {
                        // fallthrough
                    }
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class DepthPayload {
        private List<List<Double>> bids;
        private List<List<Double>> asks;
    }

    private static class KlineResult {
        private final double rsi;
        private final List<KlinePoint> klines;

        KlineResult(double rsi, List<KlinePoint> klines) {
            this.rsi = rsi;
            this.klines = klines;
        }

        double getRsi() {
            return rsi;
        }

        List<KlinePoint> getKlines() {
            return klines;
        }
    }
}
