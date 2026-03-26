package com.blibli.cartModule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

  private String errorMessage;
  private String errorCode;
  private Boolean success;
  private T value;

  public static <T> ApiResponse<T> success(T value) {
    ApiResponse<T> response = new ApiResponse<>();
    response.setSuccess(true);
    response.setValue(value);
    response.setErrorMessage(null);
    response.setErrorCode(null);
    return response;
  }

  public static <T> ApiResponse<T> error(String errorMessage, String errorCode) {
    ApiResponse<T> response = new ApiResponse<>();
    response.setSuccess(false);
    response.setErrorMessage(errorMessage);
    response.setErrorCode(errorCode);
    response.setValue(null);
    return response;
  }
}

