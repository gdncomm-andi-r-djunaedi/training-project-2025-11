package com.zasura.apiGateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {
  private Integer code;
  private String status;
  private Boolean success;
  private T data;
  private Object errorMessage;

  public static <T> CommonResponse<T> success(T data) {
    return new CommonResponse<>(HttpStatus.OK.value(), HttpStatus.OK.name(), true, data, null);
  }
  public static <T> CommonResponse<T> unauthorize(T data) {
    return new CommonResponse<>(HttpStatus.OK.value(), HttpStatus.OK.name(), true, data, null);
  }
}
