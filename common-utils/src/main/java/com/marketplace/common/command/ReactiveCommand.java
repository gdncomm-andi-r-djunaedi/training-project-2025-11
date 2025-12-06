package com.marketplace.common.command;

import reactor.core.publisher.Mono;

/**
 * Reactive Command interface for Spring WebFlux.
 * 
 * @param <T> Request type
 * @param <R> Response type
 */
public interface ReactiveCommand<T, R> {
    Mono<R> execute(T request);
}
