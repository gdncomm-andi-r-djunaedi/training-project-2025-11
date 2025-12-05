package com.example.commandlib;

@FunctionalInterface
public interface Command<T> {
    T execute() throws Exception;
}
