package com.gdn.marketplace.member.command;

public interface Command<R, T> {
    R execute(T request);
}
