package com.kmatrokhin.uvbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LocationInfo {
    private String name;
    private Coordinates coordinates;
    private UvIndex uvIndex;
}
