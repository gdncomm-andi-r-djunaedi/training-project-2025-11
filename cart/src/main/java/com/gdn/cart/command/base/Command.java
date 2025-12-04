package com.gdn.cart.command.base;

public interface Command<R, T> {

  T execute(R commandRequest);
}

