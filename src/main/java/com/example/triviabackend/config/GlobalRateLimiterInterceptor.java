package com.example.triviabackend.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;

@Component
@Log4j2
public class GlobalRateLimiterInterceptor implements HandlerInterceptor {

	private final RateLimiter limiter;

	public GlobalRateLimiterInterceptor() {
		this.limiter = RateLimiter.of("global", RateLimiterConfig.custom()
				.limitForPeriod(10)
				.limitRefreshPeriod(Duration.ofSeconds(1))
				.timeoutDuration(Duration.ofMillis(0))
				.build());
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		log.warn("Too many request executed too fast..");
		if (!limiter.acquirePermission()) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.getWriter().write("Too many requests. Please wait a minute and try again.");
			return false;
		}
		return true;
	}
}
