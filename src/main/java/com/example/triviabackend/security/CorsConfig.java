package com.example.triviabackend.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class CorsConfig implements WebMvcConfigurer {
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins(
						"http://localhost:4200", // ✅ pentru dezvoltare locală
						"https://trivia-ui-delta.vercel.app" // ✅ pentru Vercel
				)
				.allowedMethods("*")
				.allowedHeaders("*");
	}
}
