package com.r_mutt.tucine;

import com.r_mutt.tucine.principal.Principal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TucineApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(TucineApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal();
		principal.muestraElMenu();
	}
}
