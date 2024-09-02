package com.project.model;

public class ErrorResult {
    private String type;
    private String message;


    public ErrorResult() {
        this.type = "error";
    }

    public ErrorResult(String message) {
        this.type = "error";
        this.message = message;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}