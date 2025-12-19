package ru.practicum.moviehub.api;

public class ResponseException extends RuntimeException {
    private final int statusCode;

    public ResponseException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}