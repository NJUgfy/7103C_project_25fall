package com.datamining.ssedemo.controller;

import com.datamining.ssedemo.dto.GetChatIdsResp;
import com.datamining.ssedemo.dto.ContextResp;
import com.datamining.ssedemo.dto.MessageVO;
import com.datamining.ssedemo.dto.ResultVO;
import com.datamining.ssedemo.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai/history")
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryController {
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatMemory chatMemory;

    /**
     * 获取聊天列表，刷新页面时调用
     * @return
     */
    @GetMapping("/getChatIds")
    public ResultVO<GetChatIdsResp> getChatIds() {
        List<String> chatIds = chatHistoryRepository.getChatIds();
        GetChatIdsResp resp = new GetChatIdsResp();
        resp.setChatIds(chatIds);
        return ResultVO.success(resp);
    }

    /**
     * 获取该聊天上下文
     * @param chatId
     * @return
     */
    @GetMapping("get/{chatId}")
    public ResultVO<ContextResp> getChatHistory(@PathVariable("chatId") String chatId) {
        List<Message> messages = chatMemory.get(chatId);
        List<MessageVO> list = messages.stream().map(MessageVO::new).toList();
        ContextResp resp = new ContextResp();
        resp.setMessages(list);
        resp.setChatId(chatId);
        return ResultVO.success(resp);
    }
}
