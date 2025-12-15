package com.wijaya.commerce.member.commandImpl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import com.wijaya.commerce.member.command.Command;
import com.wijaya.commerce.member.command.CommandExecutor;

@Component
public class DefaultExecutor implements CommandExecutor, ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public <R, T> T execute(Class<? extends Command<R, T>> command, R request) {
        return context.getBean(command).doCommand(request);
    }

    @Override
    public void setApplicationContext(ApplicationContext context)
            throws BeansException {
        this.context = context;
    }

}
