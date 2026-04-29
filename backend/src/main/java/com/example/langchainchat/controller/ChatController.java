package com.example.langchainchat.controller;

import com.example.langchainchat.dto.ChatRequest;
import com.example.langchainchat.dto.ChatResponse;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final String allowedOrigins;

    public ChatController(
            ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            @Value("${app.cors.allowed-origins}") String allowedOrigins
    ) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.allowedOrigins = allowedOrigins;
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String prompt = buildPrompt(request);
        String answer = chatModel.chat(prompt);
        return new ChatResponse(answer);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(5).toMillis());
        String prompt = buildPrompt(request);

        CompletableFuture.runAsync(() -> {
            try {
                streamingChatModel.chat(prompt, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(partialResponse));
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse completeResponse) {
                        try {
                            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        emitter.completeWithError(error);
                    }
                });
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private String buildPrompt(ChatRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业、耐心的 AI 编程助手。回答要清楚、实用，代码尽量可以直接运行。\n\n");

        List<ChatRequest.MessageItem> history = request.getHistory();
        if (history != null && !history.isEmpty()) {
            sb.append("以下是历史对话：\n");
            for (ChatRequest.MessageItem item : history) {
                if (item == null || item.getContent() == null || item.getContent().isBlank()) {
                    continue;
                }
                String role = item.getRole() == null ? "user" : item.getRole();
                sb.append(role).append(": ").append(item.getContent()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("用户当前问题：").append(request.getMessage());
        return sb.toString();
    }
}
