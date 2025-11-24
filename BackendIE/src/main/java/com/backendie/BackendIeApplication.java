package com.backendie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class BackendIeApplication {

	public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BackendIeApplication.class);
        app.addListeners(new EnvLogger());
        app.run(args);
	}
}
