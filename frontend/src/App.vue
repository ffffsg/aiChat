<script setup>
import { computed, nextTick, ref } from "vue";
import { chat, streamChat } from "./api/chat";

const input = ref("");
const loading = ref(false);
const useStream = ref(true);
const messages = ref([
  {
    role: "assistant",
    content: "你好，我是 LangChain4j + Vue 聊天助手。你可以问我代码、学习、方案设计等问题。"
  }
]);

const chatBodyRef = ref(null);

const historyForApi = computed(() =>
  messages.value
    .filter((item) => item.role === "user" || item.role === "assistant")
    .map((item) => ({
      role: item.role,
      content: item.content
    }))
);

function scrollToBottom() {
  nextTick(() => {
    if (chatBodyRef.value) {
      chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
    }
  });
}

async function sendMessage() {
  const text = input.value.trim();
  if (!text || loading.value) return;

  input.value = "";
  messages.value.push({ role: "user", content: text });
  const assistantMessage = { role: "assistant", content: "" };
  messages.value.push(assistantMessage);
  loading.value = true;
  scrollToBottom();

  try {
    const history = historyForApi.value.slice(0, -2);

    if (useStream.value) {
      await streamChat(
        text,
        history,
        (token) => {
          assistantMessage.content += token;
          scrollToBottom();
        },
        () => {
          loading.value = false;
          scrollToBottom();
        }
      );
    } else {
      const result = await chat(text, history);
      assistantMessage.content = result.answer;
      loading.value = false;
      scrollToBottom();
    }
  } catch (error) {
    assistantMessage.content = `请求失败：${error.message || error}`;
    loading.value = false;
    scrollToBottom();
  }
}

function clearMessages() {
  messages.value = [
    {
      role: "assistant",
      content: "对话已清空。"
    }
  ];
}
</script>

<template>
  <main class="page">
    <section class="chat-card">
      <header class="chat-header">
        <div>
          <h1>LangChain4j Vue Chat</h1>
          <p>Java Spring Boot 后端 + Vue 3 前端</p>
        </div>

        <div class="header-actions">
          <label class="switch">
            <input v-model="useStream" type="checkbox" />
            <span>流式输出</span>
          </label>
          <button class="ghost-button" @click="clearMessages">清空</button>
        </div>
      </header>

      <div ref="chatBodyRef" class="chat-body">
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="message-row"
          :class="message.role"
        >
          <div class="bubble">
            <div class="role">
              {{ message.role === "user" ? "你" : "AI" }}
            </div>
            <div class="content">{{ message.content || "..." }}</div>
          </div>
        </div>
      </div>

      <footer class="chat-footer">
        <textarea
          v-model="input"
          placeholder="输入你的问题，Enter 发送，Shift + Enter 换行"
          rows="3"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <button :disabled="loading || !input.trim()" @click="sendMessage">
          {{ loading ? "生成中..." : "发送" }}
        </button>
      </footer>
    </section>
  </main>
</template>
