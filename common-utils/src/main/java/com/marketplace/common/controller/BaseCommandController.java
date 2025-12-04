package com.marketplace.common.controller;

import com.marketplace.common.command.Command;
import com.marketplace.common.command.CommandExecutor;
import com.marketplace.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Base controller providing common functionality for command-based controllers.
 * Provides helper methods for response building and command execution.
 */
public abstract class BaseCommandController {

    @Autowired
    protected CommandExecutor commandExecutor;

    /**
     * Execute a command and return its result.
     */
    protected <T, R> R execute(Class<? extends Command<T, R>> commandClass, T request) {
        return commandExecutor.execute(commandClass, request);
    }

    /**
     * Create a successful OK response with data.
     */
    protected <T> ResponseEntity<ApiResponse<T>> okResponse(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Create a successful OK response with message and data.
     */
    protected <T> ResponseEntity<ApiResponse<T>> okResponse(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    /**
     * Create a CREATED (201) response with message and data.
     */
    protected <T> ResponseEntity<ApiResponse<T>> createdResponse(String message, T data) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, data));
    }

    /**
     * Create a CREATED (201) response with data.
     */
    protected <T> ResponseEntity<ApiResponse<T>> createdResponse(T data) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Created successfully", data));
    }
}
