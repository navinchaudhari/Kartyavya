package com.kartyavya.report.exception;


import java.time.LocalDateTime;

import lombok.Data;


@Data
public class ErrorResponse {


    private LocalDateTime timestamp;

    private int status;

    private String message;

    private String path;


    public ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String message,
            String path) {

        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.path = path;

    }

}