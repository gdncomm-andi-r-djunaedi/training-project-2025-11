package com.gdn.marketplace.cart.command;

public interface Command<R, T> {
    R execute(T request);
}
