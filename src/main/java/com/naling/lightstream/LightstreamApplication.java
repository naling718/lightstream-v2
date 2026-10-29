package com.naling.lightstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication @EnableScheduling
public class LightstreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(LightstreamApplication.class, args);
	}

}
