package com.datamining.ssedemo.controller;

import com.datamining.ssedemo.dto.ChatReq;
import com.datamining.ssedemo.repository.ChatHistoryRepository;
import com.datamining.ssedemo.service.ChatWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {
    private final ChatClient chatClient;
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatWorkflowService chatWorkflowService;

    /**
     * 接收消息
     * @param
     * @return
     */
    @PostMapping(
            value = "/chat",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> chat(@RequestBody ChatReq req) {
        // 保存id
        chatHistoryRepository.save(req.getChatId());
        String userText = req.getContent();
        if (userText == null || userText.isBlank()) {
            userText = req.getMessage();
        }
        // Flux<String> content = chatClient.prompt()
        //         .user(userText)
        //         .advisors(a -> a.param(CONVERSATION_ID, req.getChatId()))
        //         .stream()
        //         .content();
        // return content;

        // 改为复用工作流，输出 extract/news/market/final 统一事件流
        return chatWorkflowService.process(req);
    }
}
