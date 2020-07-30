package com.example.resilience.config;

import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class CircuitBreakerPredicate implements Predicate<Throwable>{

	@Override
	public boolean test(Throwable throwable) {
		return throwable instanceof TimeoutException || throwable instanceof TimeoutException;
	}

}
