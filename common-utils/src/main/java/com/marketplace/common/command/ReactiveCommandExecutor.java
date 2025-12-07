package com.marketplace.common.command;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Executes reactive commands by class type.
 * For use in Spring WebFlux (API Gateway).
 */
@Component
@RequiredArgsConstructor
public class ReactiveCommandExecutor {

    private final ApplicationContext applicationContext;

    /**
     * Execute a reactive command by its class type.
     *
     * @param commandClass The command class to execute
     * @param request      The request object
     * @return Mono containing the command's response
     */
    @SuppressWarnings("unchecked")
    public <T, R> Mono<R> execute(Class<? extends ReactiveCommand<T, R>> commandClass, T request) {
        ReactiveCommand<T, R> command = applicationContext.getBean(commandClass);
        return command.execute(request);
    }
}
