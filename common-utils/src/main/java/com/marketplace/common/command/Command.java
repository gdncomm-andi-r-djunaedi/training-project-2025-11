package com.marketplace.common.command;

public interface Command<R> {
    R execute();
}
