package com.evdealer.evdealermanagement;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.evdealer.evdealermanagement.repository")
// Tắt auto-scan Redis repositories
@EnableRedisRepositories(
		basePackages = "com.evdealer.evdealermanagement.redis",
		enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.OFF,
		considerNestedRepositories = false
)
public class EvDealerManagementSystemApplication {
	public static void main(String[] args) {
		// Load .env file
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing() // Không throw error nếu .env không tồn tại
				.load();

		// Set các biến môi trường từ .env vào System properties
		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
		);

		SpringApplication.run(EvDealerManagementSystemApplication.class, args);
	}
}