package com.example.triviabackend.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChatRequestDto {
	private String model;
	private List<Message> messages;
	private double temperature;
	private int max_tokens;

	public ChatRequestDto(String model, List<Message> messages, double temperature, int max_tokens) {
		this.model = model;
		this.messages = messages;
		this.temperature = temperature;
		this.max_tokens = max_tokens;
	}

	public static class Message {
		private String role;
		private String content;

		public Message() {}

		public Message(String role, String content) {
			this.role = role;
			this.content = content;
		}

		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}

}
