package com.example.common.command;

@FunctionalInterface
public interface Command<R> {
  R execute();
}
