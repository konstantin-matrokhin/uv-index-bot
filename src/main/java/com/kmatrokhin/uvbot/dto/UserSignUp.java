package com.kmatrokhin.uvbot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignUp {
    private Long chatId;
    private String name;
//    private String buttonsMessageId;
    private LocationInfo locationInfo;
}
