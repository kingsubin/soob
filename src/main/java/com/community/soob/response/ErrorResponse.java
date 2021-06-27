package com.community.soob.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ErrorResponse {
    private final String message;
    private final String data;

    public static ErrorResponse of(String message, String data) {
        return new ErrorResponse(message, data);
    }
}
