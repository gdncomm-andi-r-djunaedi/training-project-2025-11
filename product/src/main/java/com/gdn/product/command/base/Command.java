package com.gdn.product.command.base;

public interface Command<R, T> {

  T execute(R commandRequest);
}
