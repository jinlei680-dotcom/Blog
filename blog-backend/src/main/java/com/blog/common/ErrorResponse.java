package com.blog.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int code;
    private String message;
    private LocalDateTime timestamp;

    public static ErrorResponse of(int code, String message) {
        return new ErrorResponse(code, message, LocalDateTime.now());
    }
}
