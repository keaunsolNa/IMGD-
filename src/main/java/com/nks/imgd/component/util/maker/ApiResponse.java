package com.nks.imgd.component.util.maker;


import com.nks.imgd.dto.Enum.ResponseMsg;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ApiError error;
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.data = data;
        r.timestamp = Instant.now();
        return r;
    }

    public static <T> ApiResponse<T> error(ResponseMsg responseMsg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.error = new ApiError(responseMsg.getKey(), responseMsg.getMsg(), null != responseMsg.getDetails() ? responseMsg.getDetails() : null);
        r.timestamp = Instant.now();
        return r;
    }

    @Getter
    @AllArgsConstructor
    public static class ApiError {
        private String code;
        private String message;
        private Map<String, Object> details;
    }

}
