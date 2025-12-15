package com.wijaya.commerce.member.command;

public interface CommandExecutor {
    <R, T> T execute(Class<? extends Command<R, T>> command, R request);
}
