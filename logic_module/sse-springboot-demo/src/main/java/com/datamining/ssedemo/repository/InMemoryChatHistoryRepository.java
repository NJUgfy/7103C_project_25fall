package com.datamining.ssedemo.repository;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 存入内存配置
 */
@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository {
    private final List<String> chatHistory = new ArrayList<>();

    @Override
    public void save(String chatId) {
        if (chatHistory.contains(chatId)) {
            return;
        }
        chatHistory.add(chatId);
    }

    @Override
    public List<String> getChatIds() {
        return Collections.unmodifiableList(chatHistory);
    }
}
