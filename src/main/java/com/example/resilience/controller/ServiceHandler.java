package com.example.resilience.controller;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.resilience.service.AService;
import com.example.resilience.service.BService;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.vavr.control.Try;
import reactor.core.publisher.Mono;

@Component
public class ServiceHandler {

	@Autowired
	private AService aService;
	
	@Autowired
	private BService bService;
	
	@Autowired
	private CircuitBreaker serviceBCircuitBreaker;
	
	private static final Logger LOG = LoggerFactory.getLogger(ServiceHandler.class);
	
	public Mono<ServerResponse> getServiceA(ServerRequest request){
		String i = request.pathVariable("id");
		
		try {
			
			aService.testServiceA(Integer.parseInt(i));
			
		} catch (HttpClientErrorException e) {
			LOG.error("HttpClientErrorException with {}", i);
			LOG.error("HttpClientErrorException message {}", e.getMessage());
			return ServerResponse.status(HttpStatus.LOCKED).build();
			
		} catch (TimeoutException e) {
			LOG.error("TimeoutException with {}", i);
			LOG.error("TimeoutException message {}", e.getMessage());
			return ServerResponse.status(HttpStatus.GATEWAY_TIMEOUT).build();
			
		} catch (CallNotPermittedException e) {
			LOG.error("CallNotPermittedException with {}", i);
			LOG.error("CallNotPermittedException message {}", e.getMessage());
			return ServerResponse.status(HttpStatus.ALREADY_REPORTED).body(Mono.just("AService is in maintianance."), String.class);
		}
		//return Mono.just(result).flatMap(r -> ServerResponse.ok().body(Mono.just(result), String.class));
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Mono.just("Complete getServiceA Handler."), String.class);
	}
	
	public Mono<ServerResponse> getServiceB(ServerRequest request){
		String i = request.pathVariable("id");
		
		try {
			
			Callable<String> callable = serviceBCircuitBreaker.decorateCallable(() -> bService.testServiceB(Integer.parseInt(i)));
			String result = callable.call();
			LOG.info(result);
			
			//Try.ofCallable(callable).recover()
			
//			ExecutorService service = Executors.newSingleThreadExecutor();
//			LOG.info(service.submit(callable).get());
			
		} catch (HttpClientErrorException e) {
			LOG.error("HttpClientErrorException with {}", i);
			LOG.error("HttpClientErrorException message {}", e.getMessage());
			return ServerResponse.status(HttpStatus.BAD_GATEWAY).build();
		} catch (Exception e) {
			LOG.error("Exception with {}", i);
			LOG.error("Exception message {}", e.getMessage());
			return ServerResponse.status(HttpStatus.ALREADY_REPORTED).body(Mono.just("BService is in maintianance."), String.class);
		}

		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Mono.just("Complete getServiceB Handler."), String.class);
	}
}
