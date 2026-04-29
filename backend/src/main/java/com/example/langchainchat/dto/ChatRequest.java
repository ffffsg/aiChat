package com.example.langchainchat.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class ChatRequest {

    @NotBlank(message = "message 不能为空")
    private String message;

    private List<MessageItem> history = new ArrayList<>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<MessageItem> getHistory() {
        return history;
    }

    public void setHistory(List<MessageItem> history) {
        this.history = history == null ? new ArrayList<>() : history;
    }

    public static class MessageItem {

        /**
         * role 可选：user / assistant / system
         */
        private String role;

        private String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
