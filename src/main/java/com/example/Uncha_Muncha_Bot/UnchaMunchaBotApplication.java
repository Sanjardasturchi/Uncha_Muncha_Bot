package com.example.Uncha_Muncha_Bot;

import io.github.nazarovctrl.telegrambotspring.annotation.EnableTelegramLongPollingBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@EnableTelegramLongPollingBot
@SpringBootApplication
public class UnchaMunchaBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnchaMunchaBotApplication.class, args);
		System.out.println("App start");
	}

}
