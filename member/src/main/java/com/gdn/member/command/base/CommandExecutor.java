package com.gdn.member.command.base;

public interface CommandExecutor {
  <R, T> T execute(Class<? extends Command<R, T>> commandClass, R request);
}
