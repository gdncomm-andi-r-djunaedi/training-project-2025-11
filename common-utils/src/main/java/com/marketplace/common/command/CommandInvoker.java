package com.marketplace.common.command;

import org.springframework.stereotype.Component;

@Component
public class CommandInvoker {

    public <R> R executeCommand(Command<R> command) {
        return command.execute();
    }
}
