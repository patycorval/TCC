package com.bd.sitebd;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class SitebdApplication {

	public static void main(String[] args) {
		SpringApplication.run(SitebdApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo() {
		return args -> {
			String senhaCriptografada = new BCryptPasswordEncoder().encode("123");
			System.out.println("Senha gerada: " + senhaCriptografada);
		};
	}

}