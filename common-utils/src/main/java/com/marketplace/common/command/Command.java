package com.marketplace.common.command;

/**
 * Generic Command interface with request and response types.
 * Commands are Spring-managed beans.
 * 
 * @param <T> Request type
 * @param <R> Response type
 */
public interface Command<T, R> {
    R execute(T request);
}
