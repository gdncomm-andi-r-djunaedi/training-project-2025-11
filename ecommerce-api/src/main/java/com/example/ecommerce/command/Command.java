package com.example.ecommerce.command;

/**
 * Generic command interface for encapsulating operations.
 * 
 * @param <T> The return type of the command execution
 */
public interface Command<T> {
    /**
     * Execute the command and return the result.
     * 
     * @return The result of the command execution
     */
    T execute();
}
