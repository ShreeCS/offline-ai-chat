# Offline Ai Chat

Spring Boot + Spring AI + Ollama starter for private, offline LLM chat. It exposes:

- Non‑streaming chat: simple request/response
- Streaming chat: Server-Sent Events (SSE) token stream

Works entirely on your machine using an Ollama model (default: `llama3.1:8b`). No cloud keys required.

> Repository name: "offline-ai-chat"

## Demo

https://github.com/user-attachments/assets/3af67d83-2afe-4a47-be46-85c48e53dec5

## Features

- Spring Boot 3 (Java 17)
- Spring AI Ollama integration
- REST + SSE endpoints
- CORS enabled (CrossOrigin("*")) for quick prototyping

## Prerequisites

- Java 17 (JDK)
- Maven (uses included wrapper `mvnw.cmd`)
- Ollama installed and running locally
  - Download/install: <https://ollama.com> (Windows installer)
  - Pull a model (default used by this app):

```bat
ollama pull llama3.1:8b
```

If you prefer a different model, update `src/main/resources/application.properties` accordingly.

## Quick start (dev)

1. Ensure Ollama is running and the model is available

- Ollama serves on <http://localhost:11434> by default.
- Warm up the model (optional):

```bat
ollama run llama3.1:8b
```

2. Run the Spring Boot app

- Using Maven Wrapper (recommended):

```bat
mvnw.cmd clean spring-boot:run
```

The app starts on <http://localhost:8080>.

Note on packaging: The project is configured as `war` with `spring-boot-starter-tomcat` scope `provided` for container deployments. `spring-boot:run` works for local development. If you want a standalone executable, switch to `jar` packaging or deploy the generated `war` to an external Tomcat (10+).

## Build

- Build the artifact:

```bat
mvnw.cmd clean package
```

- Result: `target/ollama-0.0.1-SNAPSHOT.war`
- Deploy to Tomcat 10+ or any compatible Jakarta Servlet container.

## Configuration

File: `src/main/resources/application.properties`

```properties
spring.application.name=ollama
spring.ai.ollama.chat.options.model=llama3.1:8b
```

Environment overrides (Windows cmd examples):

```bat
set SPRING_AI_OLLAMA_BASE_URL=http://localhost:11434
set SPRING_AI_OLLAMA_CHAT_OPTIONS_MODEL=llama3.1:8b
```

## API

Base URL: `http://localhost:8080/api/ollama`

- Non‑streaming
  - GET `/{message}`
  - Returns: plain text response from the model

Example:

```bat
curl "http://localhost:8080/api/ollama/Hello%20there"
```

- Streaming (SSE) ⭐
  - GET `/stream/{message}` with `text/event-stream`
  - Emits events named `message` for tokens and `error` on failure

Quick test (prints streamed tokens):

```bat
curl -N "http://localhost:8080/api/ollama/stream/Tell%20me%20a%20joke"
```

### Consume SSE from a browser

```html
<!doctype html>
<html>
  <body>
    <input id="q" placeholder="Ask something" />
    <button onclick="go()">Ask</button>
    <pre id="out"></pre>
    <script>
      function go() {
        const msg = encodeURIComponent(document.getElementById('q').value || 'Hello');
        const es = new EventSource(`/api/ollama/stream/${msg}`);
        const out = document.getElementById('out');
        out.textContent = '';
        es.addEventListener('message', e => out.textContent += e.data);
        es.addEventListener('error', e => { out.textContent += '\n[error]'; es.close(); });
      }
    </script>
  </body>
</html>
```

## Project layout

- `pom.xml` – Spring Boot 3 app, Spring AI Ollama starter
- `src/main/java/com/shree/ollama/controllers/OllamaController.java` – REST + SSE endpoints
- `src/main/resources/application.properties` – model and app config

## Troubleshooting

- Connection refused to `http://localhost:11434` – Start Ollama service and ensure the model is pulled.
- Slow first response – Cold start; run `ollama run <model>` once to warm up.
- SSE not streaming in some clients – Ensure client supports `text/event-stream` and doesn’t buffer (proxies may buffer). `curl -N` disables buffering.

## Security

- CORS is wide open for ease of testing (`@CrossOrigin("*")`). Lock it down before production.

## Acknowledgements

- Spring Boot – <https://spring.io/projects/spring-boot>
- Spring AI – <https://spring.io/projects/spring-ai>
- Ollama – <https://ollama.com>
