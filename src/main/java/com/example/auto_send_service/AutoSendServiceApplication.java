package com.example.auto_send_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AutoSendServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoSendServiceApplication.class, args);
	}

}
