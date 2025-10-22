package com.datamining.ssedemo.controller;

import com.datamining.ssedemo.dto.ChatReq;
import com.datamining.ssedemo.repository.ChatHistoryRepository;
import com.datamining.ssedemo.service.ChatWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Instant;

/**
 * 基于工作流的 SSE 接口。
 */
@Slf4j
@RestController
@RequestMapping("/ai/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final ChatWorkflowService chatWorkflowService;
    private final ChatHistoryRepository chatHistoryRepository;

    @PostMapping(value = "/chat",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatReq req) {
        ensureChatId(req);
        chatHistoryRepository.save(req.getChatId());
        return chatWorkflowService.process(req);
    }

    private void ensureChatId(ChatReq req) {
        if (!StringUtils.hasText(req.getChatId())) {
            req.setChatId("chat-" + Instant.now().toEpochMilli());
        }
    }
}
