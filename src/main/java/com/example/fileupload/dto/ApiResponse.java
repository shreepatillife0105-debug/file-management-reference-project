package com.example.fileupload.dto;

/*
 * Common response wrapper used by every API.
 *
 * Keeping one response format makes Angular/frontend integration easier.
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;
    private Object meta;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data,
                       String error, Object meta) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
        this.meta = meta;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, null);
    }

    public static <T> ApiResponse<T> failure(String message, String error) {
        return new ApiResponse<>(false, message, null, error, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getMeta() {
        return meta;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }
}
