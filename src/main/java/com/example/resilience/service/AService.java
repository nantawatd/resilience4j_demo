package com.example.resilience.service;

import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class AService {

	@CircuitBreaker(name = "serviceA")
	public String testServiceA(int i) throws TimeoutException {
		
		if(i == 500) {
			throw new TimeoutException("Time out");
		}
		
		if (i % 2 == 0) {
			throw new HttpClientErrorException(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
		}

		return "complete testServiceA";
	}
}
