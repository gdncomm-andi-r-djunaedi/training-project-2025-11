package com.blibli.member.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GdnResponse<T> {
    private Boolean success;
    private String errorMessage;
    private T data;

    public GdnResponse(Boolean success,String errorMessage,T data){
        this.success=true;
        this.data =data;
        this.errorMessage=errorMessage;
    }
    public GdnResponse(String errorMessage){
        this.success =false;
        this.errorMessage = errorMessage;
    }

    public static GdnResponse error(String message) {
        return new GdnResponse(message);
    }
}
