const API_BASE = import.meta.env.VITE_API_BASE || "";

export async function chat(message, history) {
  const response = await fetch(`${API_BASE}/api/chat`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ message, history })
  });

  if (!response.ok) {
    throw new Error(await response.text());
  }

  return response.json();
}

export async function streamChat(message, history, onToken, onDone) {
  const response = await fetch(`${API_BASE}/api/chat/stream`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ message, history })
  });

  if (!response.ok || !response.body) {
    throw new Error(await response.text());
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();

    if (done) {
      onDone?.();
      break;
    }

    buffer += decoder.decode(value, { stream: true });

    const events = buffer.split("\n\n");
    buffer = events.pop() || "";

    for (const event of events) {
      const lines = event.split("\n");
      for (const line of lines) {
        if (!line.startsWith("data:")) continue;

        const data = line.replace(/^data:\s?/, "");
        if (data === "[DONE]") {
          onDone?.();
          return;
        }

        onToken(data);
      }
    }
  }
}
