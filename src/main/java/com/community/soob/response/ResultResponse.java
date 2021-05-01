package com.community.soob.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResultResponse<T> {
    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL"; // 400
    public static final String ERROR = "ERROR"; // 500

    private T data;
    private String status;
    private String message;

    public ResultResponse(String status) {
        this.status = status;
    }

    public ResultResponse(String status, T data) {
        this.status = status;
        this.data = data;
    }

    public ResultResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static <T> ResultResponse<T> of(String status, T data) {
        return new ResultResponse<>(
                status, data
        );
    }

    public static ResultResponse<Void> of(String status) {
        return new ResultResponse<>(
                status
        );
    }

    public static ResultResponse<Void> of(String status, String message) {
        return new ResultResponse<>(
                status, message
        );
    }
}
