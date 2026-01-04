package CartService.CartService.common;

public class ResponseUtil {
        public static <T> ApiResponse<T> success( T data){
            return ApiResponse.<T>builder()
                    .success(true)
                    .data(data)
                    .build();
        }

        public static ApiResponse<?> error(String message,String code){
            return ApiResponse.builder()
                    .success(false)
                    .errorCode(code)
                    .errorMsg(message)
                    .build();

        }

}
