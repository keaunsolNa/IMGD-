package com.nks.imgd.component.util.maker;

import com.nks.imgd.dto.Enum.ResponseMsg;

import java.util.Objects;
import java.util.function.Supplier;

public record ServiceResult<T>(ResponseMsg status, Supplier<T> onSuccess) {

    public ServiceResult(ResponseMsg status, Supplier<T> onSuccess) {
        this.status = Objects.requireNonNull(status, "status");
        this.onSuccess = onSuccess;
    }

    public static <T> ServiceResult<T> success(Supplier<T> onSuccess) {
        return new ServiceResult<>(ResponseMsg.ON_SUCCESS, Objects.requireNonNull(onSuccess, "onSuccess"));
    }

    public static <T> ServiceResult<T> failure(ResponseMsg status) {
        if (status == ResponseMsg.ON_SUCCESS) {
            throw new IllegalArgumentException("Use success(...) for ON_SUCCESS");
        }
        return new ServiceResult<>(status, null);
    }

}
