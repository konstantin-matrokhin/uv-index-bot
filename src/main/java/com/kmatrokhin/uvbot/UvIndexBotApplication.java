package com.kmatrokhin.uvbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UvIndexBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(UvIndexBotApplication.class, args);
	}

}
