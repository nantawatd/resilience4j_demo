package com.example.resilience.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import com.example.resilience.service.AService;

@RestController
public class ServiceController {

	@Autowired
	private AService aService;
	
	private static final Logger LOG = LoggerFactory.getLogger(ServiceController.class);
	
	@GetMapping("/aservice/{i}")
	public ResponseEntity<String> getAService(@PathVariable int i){
		LOG.info("getAService with {}", i);
		try {
			aService.testServiceA(i);
		} catch (HttpClientErrorException e) {
			LOG.error("HttpClientErrorException with {}", i);
			LOG.error("HttpClientErrorException message {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
		} catch (Exception e) {
			LOG.error("Exception with {}", i);
			LOG.error("Exception message {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body("AService is in maintianance.");
		}
		return ResponseEntity.ok().body("i =" + i);
	}
}
