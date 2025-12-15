package com.blibli.search.exception;

public class ElasticsearchException extends RuntimeException {
    public ElasticsearchException(String message) {
        super(message);
    }

    public ElasticsearchException(String message, Throwable cause) {
        super(message, cause);
    }
}

