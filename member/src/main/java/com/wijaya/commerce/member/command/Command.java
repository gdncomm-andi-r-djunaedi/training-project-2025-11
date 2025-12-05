package com.wijaya.commerce.member.command;

public interface Command<R, T> {
    T doCommand(R request);
}
