package com.kmatrokhin.uvbot.chatgpt;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private int n = 1;
    private double temperature = 1.2;

    public ChatRequest(String model, String sysPrompt, String userPrompt) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("system", sysPrompt));
        this.messages.add(new Message("user", userPrompt));
    }
}
