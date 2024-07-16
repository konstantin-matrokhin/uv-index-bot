package com.kmatrokhin.uvbot.chatgpt;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Message {
    private String role;
    private String content;
}
