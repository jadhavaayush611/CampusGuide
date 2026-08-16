package com.campusguide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class CampusguideApplication {

	public static void main(String[] args) {
		loadEnvFile();
		SpringApplication.run(CampusguideApplication.class, args);
	}

	private static void loadEnvFile() {
		Path envPath = Paths.get(".env");
		if (!Files.exists(envPath)) {
			envPath = Paths.get("backend", ".env");
		}
		if (Files.exists(envPath)) {
			try {
				List<String> lines = Files.readAllLines(envPath);
				for (String line : lines) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						continue;
					}
					int eqIdx = line.indexOf('=');
					if (eqIdx > 0) {
						String key = line.substring(0, eqIdx).trim();
						String value = line.substring(eqIdx + 1).trim();
						if ((value.startsWith("\"") && value.endsWith("\"")) || 
							(value.startsWith("'") && value.endsWith("'"))) {
							value = value.substring(1, value.length() - 1);
						}
						if (System.getProperty(key) == null && System.getenv(key) == null) {
							System.setProperty(key, value);
						}
					}
				}
			} catch (IOException e) {
				System.err.println("Failed to load .env file: " + e.getMessage());
			}
		}
	}
}
