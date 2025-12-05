package com.zasura.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse {
  private Integer code;
  private String status;
  private Boolean success;
  private Object data;
  private Object errorMessage;
}
