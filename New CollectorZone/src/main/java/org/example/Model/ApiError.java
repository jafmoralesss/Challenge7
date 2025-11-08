package org.example.Model;

public class ApiError {
    private String error;

    public ApiError(String message) {
        this.error = message;
    }

    public String getError() {
        return error;
    }
}