package com.marketplace.common.command;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Executes commands by class type.
 * Commands are fetched from Spring ApplicationContext.
 */
@Component
@RequiredArgsConstructor
public class CommandExecutor {

    private final ApplicationContext applicationContext;

    /**
     * Execute a command by its class type.
     * 
     * @param commandClass The command class to execute
     * @param request      The request object
     * @return The command's response
     */
    @SuppressWarnings("unchecked")
    public <T, R> R execute(Class<? extends Command<T, R>> commandClass, T request) {
        Command<T, R> command = applicationContext.getBean(commandClass);
        return command.execute(request);
    }
}
