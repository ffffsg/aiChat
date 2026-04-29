# LangChain4j + Java Spring Boot + Vue 聊天项目

这是一个可直接运行的前后端项目：

- 后端：Java 17 + Spring Boot 3.5 + LangChain4j
- 前端：Vue 3 + Vite
- 模型：OpenAI 兼容模型
- 功能：普通聊天、流式聊天、跨域配置、聊天历史 UI

> Java 生态里通常用 **LangChain4j**，它是 LangChain 思路在 Java 里的实现。官方 Spring Boot 集成推荐使用 `langchain4j-open-ai-spring-boot-starter`。

## 目录结构

```txt
langchain4j-vue-chat/
├─ backend/
│  ├─ pom.xml
│  └─ src/main/
│     ├─ java/com/example/langchainchat/
│     │  ├─ LangchainChatApplication.java
│     │  ├─ controller/ChatController.java
│     │  └─ dto/
│     │     ├─ ChatRequest.java
│     │     └─ ChatResponse.java
│     └─ resources/
│        ├─ application.yml
│        └─ application-example.yml
└─ frontend/
   ├─ package.json
   ├─ index.html
   ├─ vite.config.js
   └─ src/
      ├─ main.js
      ├─ App.vue
      ├─ style.css
      └─ api/chat.js
```

## 1. 启动后端

进入后端目录：

```bash
cd backend
```

设置环境变量：

Windows PowerShell：

```bash
$env:OPENAI_API_KEY="你的 OpenAI API Key"
```

macOS / Linux：

```bash
export OPENAI_API_KEY="你的 OpenAI API Key"
```

启动：

```bash
mvn spring-boot:run
```

后端默认地址：

```txt
http://localhost:8080
```

健康检查：

```txt
http://localhost:8080/api/health
```

## 2. 启动前端

新开一个终端：

```bash
cd frontend
npm install
npm run dev
```

前端地址：

```txt
http://localhost:5173
```

## 3. 配置模型

后端配置在：

```txt
backend/src/main/resources/application.yml
```

默认模型：

```yml
langchain4j:
  open-ai:
    chat-model:
      model-name: gpt-4o-mini
    streaming-chat-model:
      model-name: gpt-4o-mini
```

你可以改成自己账户可用的模型。

## 4. 接口说明

### 普通聊天

```http
POST /api/chat
Content-Type: application/json
```

请求体：

```json
{
  "message": "你好，介绍一下 LangChain4j",
  "history": [
    {
      "role": "user",
      "content": "你是谁？"
    },
    {
      "role": "assistant",
      "content": "我是 AI 助手。"
    }
  ]
}
```

返回：

```json
{
  "answer": "..."
}
```

### 流式聊天

```http
POST /api/chat/stream
Content-Type: application/json
```

返回类型：

```txt
text/event-stream
```

前端已经写好 `ReadableStream` 读取逻辑，会边生成边显示。

## 5. 常见问题

### Maven 下载很慢

可以配置 Maven 国内镜像，或者在 IDE 里重新刷新 Maven。

### OPENAI_API_KEY 没生效

确认你是在启动后端的同一个终端里设置的环境变量。

### 前端请求失败

确认后端 `8080` 正在运行，并且 `application.yml` 允许了：

```yml
app:
  cors:
    allowed-origins: http://localhost:5173
```

### 想接本地模型

可以把 LangChain4j 的 OpenAI starter 换成 Ollama starter，然后修改配置为 Ollama 的地址和模型名。

## 6. 可扩展方向

后面可以继续加：

- MySQL / PostgreSQL 存储聊天记录
- Redis 会话记忆
- RAG 文档问答
- 文件上传
- JWT 登录
- 多会话侧边栏
- Docker Compose 部署
