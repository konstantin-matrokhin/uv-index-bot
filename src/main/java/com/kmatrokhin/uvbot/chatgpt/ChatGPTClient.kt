package com.kmatrokhin.uvbot.chatgpt;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "chatGPTClient", url = "https://api.openai.com/v1")
public interface ChatGPTClient {
    @PostMapping("/chat/completions")
    ChatResponse getChatCompletion(
        @RequestHeader("Authorization") String authorization,
        @RequestBody ChatRequest request
    );
}
