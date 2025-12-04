package com.zasura.product.dto;

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
  private Pagination pagination;

  public static <T> CommonResponse<T> success(T data) {
    return new CommonResponse<>(HttpStatus.OK.value(),
        HttpStatus.OK.name(),
        true,
        data,
        null,
        null);
  }

  public static <T> CommonResponse<T> successWithPagination(T data, Pagination pagination) {
    return new CommonResponse<>(HttpStatus.OK.value(),
        HttpStatus.OK.name(),
        true,
        data,
        null,
        pagination);
  }
}
