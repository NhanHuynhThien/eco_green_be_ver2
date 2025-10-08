package com.evdealer.evdealermanagement;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EvDealerManagementSystemApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		String jwtSecret = dotenv.get("JWT_SECRET");
		if (jwtSecret == null) {
			throw new RuntimeException("JWT_SECRET not defined in .env");
		}
		System.setProperty("JWT_SECRET", jwtSecret);

		String jwtExp = dotenv.get("JWT_EXPIRATION", "10800000"); // default nếu null
		System.setProperty("JWT_EXPIRATION", jwtExp);

		SpringApplication.run(EvDealerManagementSystemApplication.class, args);
	}
}

