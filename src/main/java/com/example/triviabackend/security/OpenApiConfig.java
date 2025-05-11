package com.example.triviabackend.security;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "Trivia Quiz API",
				version = "1.0",
				description = "API pentru generare intrebari de cultura generala"
		)
)
public class OpenApiConfig {
}
