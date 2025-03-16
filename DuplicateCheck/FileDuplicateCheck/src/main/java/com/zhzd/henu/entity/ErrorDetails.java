package com.zhzd.henu.entity;

public class ErrorDetails {
    private int statusCode;
    private String message;

    public ErrorDetails(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    // getters and setters
}
