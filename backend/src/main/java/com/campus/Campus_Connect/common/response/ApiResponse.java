package com.campus.Campus_Connect.common.response;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message
) {
}