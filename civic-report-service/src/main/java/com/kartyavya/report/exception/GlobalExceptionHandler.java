package com.kartyavya.report.exception;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;



@RestControllerAdvice
public class GlobalExceptionHandler {



    // Report not found exception
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReportNotFound(
            ReportNotFoundException ex,
            HttpServletRequest request) {


        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );


        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);

    }




    // Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {


        Map<String,String> errors = new HashMap<>();


        ex.getBindingResult()
          .getFieldErrors()
          .forEach(error -> {

              errors.put(
                    error.getField(),
                    error.getDefaultMessage()
              );

          });


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);

    }



    // Generic Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {


        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                request.getRequestURI()
        );


        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);

    }


}