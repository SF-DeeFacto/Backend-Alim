package com.deefacto.alim_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AlimServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlimServiceApplication.class, args);
	}

}
