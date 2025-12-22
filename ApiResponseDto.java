package com.moviebookingapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for API response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String timestamp;
    
    @JsonProperty("path")
    private String path;
    
    // Constructors
    public ApiResponseDto() {
        this.timestamp = java.time.LocalDateTime.now().toString();
    }
    
    public ApiResponseDto(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }
    
    public ApiResponseDto(boolean success, String message, T data) {
        this(success, message);
        this.data = data;
    }
    
    // Static factory methods
    public static <T> ApiResponseDto<T> success(String message) {
        return new ApiResponseDto<>(true, message);
    }
    
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, data);
    }
    
    public static <T> ApiResponseDto<T> error(String message) {
        return new ApiResponseDto<>(false, message);
    }
    
    public static <T> ApiResponseDto<T> error(String message, T data) {
        return new ApiResponseDto<>(false, message, data);
    }
    
    // Getters and Setters
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
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    @Override
    public String toString() {
        return "ApiResponseDto{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
