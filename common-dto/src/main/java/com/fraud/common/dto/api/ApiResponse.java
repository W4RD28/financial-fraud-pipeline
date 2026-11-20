package com.fraud.common.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success, String message, T data, PageMeta pagination, Instant timestamp) {

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, "Success", data, null, Instant.now());
  }

  public static <T> ApiResponse<T> ok(String message, T data) {
    return new ApiResponse<>(true, message, data, null, Instant.now());
  }

  public static <T> ApiResponse<List<T>> paged(List<T> items, PageMeta pagination) {
    return new ApiResponse<>(true, "Success", items, pagination, Instant.now());
  }

  public static <T> ApiResponse<T> error(String message, T errorDetails) {
    return new ApiResponse<>(false, message, errorDetails, null, Instant.now());
  }
}
