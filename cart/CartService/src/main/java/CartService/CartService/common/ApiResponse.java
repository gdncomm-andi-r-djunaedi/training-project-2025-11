package CartService.CartService.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T>{

    private String errorMsg;
    private String errorCode;
    private Boolean success;
    private T data;
}
