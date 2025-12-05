package com.example.commandlib;

import java.util.concurrent.*;

public class CommandExecutor {
    private final ExecutorService executor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));

    public <T> T execute(Command<T> command) {
        try {
            Future<T> f = executor.submit(() -> command.execute());
            return f.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
