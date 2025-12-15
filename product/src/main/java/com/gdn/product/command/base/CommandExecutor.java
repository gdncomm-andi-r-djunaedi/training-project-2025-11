package com.gdn.product.command.base;

import com.gdn.product.command.base.Command;

public interface CommandExecutor {
  <R, T> T execute(Class<? extends Command<R, T>> commandClass, R request);
}
