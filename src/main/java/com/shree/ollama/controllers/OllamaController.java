package com.shree.ollama.controllers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ollama")
@CrossOrigin("*")
public class OllamaController {

    private final ChatClient chatClient;

    public OllamaController(OllamaChatModel chatModel){
        this.chatClient = ChatClient.create(chatModel);
    }

    // Existing non-streaming endpoint (optional)
    @GetMapping("/{message}")
    public String getAnswer(@PathVariable String message){
        return chatClient.prompt(message).call().content();
    }

    // New: Streaming endpoint (SSE)
    @GetMapping(value = "/stream/{message}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAnswer(@PathVariable String message) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout

        chatClient
            .prompt(message)
            .stream()
            .content()
            .doOnError(ex -> {
                try { emitter.send(SseEmitter.event().name("error").data(ex.getMessage())); }
                catch (Exception ignored) {}
                emitter.completeWithError(ex);
            })
            .doOnComplete(emitter::complete)
            .subscribe(token -> {
                try { emitter.send(SseEmitter.event().name("message").data(token)); }
                catch (Exception e) { emitter.completeWithError(e); }
            });

        return emitter;
    }
}