package com.example.resilience;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.resilience.config.GracefulShutdown;
import com.example.resilience.controller.ServiceHandler;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties;

@SpringBootApplication
public class Resilience4jProjectApplication {

	@Autowired
	private ServiceHandler handler;

	public static void main(String[] args) {
		SpringApplication.run(Resilience4jProjectApplication.class, args);
	}

	@Bean
	public RouterFunction<ServerResponse> router() {
		return RouterFunctions
				.route(RequestPredicates.GET("/aservice/{id}")
						.and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::getServiceA)
				.andRoute(RequestPredicates.GET("/bservice/{id}")
						.and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::getServiceB);
	}

	@Bean
	@Qualifier("serviceBCircuitBreaker")
	public CircuitBreaker serviceBCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry,
			CircuitBreakerProperties circuitBreakerProperties) {
		
		CircuitBreakerConfig config = circuitBreakerProperties
				.createCircuitBreakerConfig(circuitBreakerProperties.getBackendProperties("serviceB"));
		
		return circuitBreakerRegistry.circuitBreaker("serviceB", config);
	}
	
	@Bean
	public GracefulShutdown gracefulShutdown() {
	    return new GracefulShutdown();
	}

	@Bean
	public ConfigurableServletWebServerFactory webServerFactory(final GracefulShutdown gracefulShutdown) {
	    TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
	    //factory.addConnectorCustomizers(gracefulShutdown);
	    factory.addConnectorCustomizers(new GracefulShutdown());
	    return factory;
	}
}
