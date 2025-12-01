package com.example.common.command;

import org.springframework.stereotype.Component;

@Component
public class CommandExecutor {

  public <R> R execute(Command<R> command) {
    return command.execute();
  }
}
