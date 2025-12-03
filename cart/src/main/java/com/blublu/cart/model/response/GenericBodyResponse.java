package com.blublu.cart.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericBodyResponse<T> {
  private String errorMessage;
  private int errorCode;
  private boolean success;
  private List<T> content;
}
