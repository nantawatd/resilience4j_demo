package com.example.resilience.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class BService {
	
	public String testServiceB(int i) {
		if (i % 2 == 0) {
			throw new HttpClientErrorException(HttpStatus.CONFLICT);
		}

		return "complete testServiceB";
	}
	
	
}
