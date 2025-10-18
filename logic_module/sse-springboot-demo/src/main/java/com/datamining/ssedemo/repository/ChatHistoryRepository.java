package com.datamining.ssedemo.repository;

import java.util.List;

public interface ChatHistoryRepository {
    /**
     * 保存会话记录
     * @param chatId
     */
    void save(String chatId);

    /**
     * 查询会话列表
     * @return
     */
    List<String> getChatIds();
}
