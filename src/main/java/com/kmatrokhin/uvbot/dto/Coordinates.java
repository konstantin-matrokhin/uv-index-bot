package com.kmatrokhin.uvbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Coordinates {
    private Double latitude;
    private Double longitude;

    public static Coordinates of(Double latitude, Double longitude) {
        return new Coordinates(latitude, longitude);
    }

    @Override
    public String toString() {
        return "@" + latitude + "," + longitude;
    }
}
