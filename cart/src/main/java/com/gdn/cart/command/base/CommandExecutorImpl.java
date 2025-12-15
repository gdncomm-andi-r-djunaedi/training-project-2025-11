package com.gdn.cart.command.base;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandExecutorImpl implements CommandExecutor {

  private final ApplicationContext applicationContext;

  @Override
  public <R, T> T execute(Class<? extends Command<R, T>> commandClass, R request) {
    Command<R, T> command = applicationContext.getBean(commandClass);
    return command.execute(request);
  }
}

