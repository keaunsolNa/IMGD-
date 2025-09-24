package com.nks.imgd.component.util.maker;

import com.nks.imgd.dto.Enum.ResponseMsg;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author nks
 * @apiNote Service Layer 에서 반환 타입으로 지정한다.
 *          Service Layer의 비지니스 로직 수행 결과값에 맞춰 Success / Failure 상황에 맞춰 후처리를 수행한다
 *          Success -> Supplier<T> 형태의 onSuccess 함수 시행 결과 값
 *          Failure -> Enum Type ResponseMsg 형태의 결과 값 (400 / 404 ···.)
 * @param status
 * @param onSuccess
 * @param <T> status : ResponseCode
 *           onSuccess : Supplier<T> 결과 객체 </T>
 */
public record ServiceResult<T>(
	ResponseMsg status,
	Supplier<T> onSuccess,
	Map<String, Object> details
) {
	public ServiceResult {
		Objects.requireNonNull(status, "status");
		// onSuccess는 실패 케이스에서 null 허용
		details = details == null ? Map.of() : Map.copyOf(details); // 불변화
	}

	public static <T> ServiceResult<T> success(Supplier<T> onSuccess) {
		return new ServiceResult<>(ResponseMsg.ON_SUCCESS, Objects.requireNonNull(onSuccess, "onSuccess"), Map.of());
	}

	public static <T> ServiceResult<T> success(Map<String, Object> map) {
		return new ServiceResult<>(ResponseMsg.ON_SUCCESS, null, map);
	}

	public static <T> ServiceResult<T> failure(ResponseMsg status) {
		if (status == ResponseMsg.ON_SUCCESS) {
			throw new IllegalArgumentException("Use success(...) for ON_SUCCESS");
		}
		return new ServiceResult<>(status, null, Map.of());
	}

	public static <T> ServiceResult<T> failure(ResponseMsg status, Map<String, Object> details) {
		if (status == ResponseMsg.ON_SUCCESS) {
			throw new IllegalArgumentException("Use success(...) for ON_SUCCESS");
		}
		return new ServiceResult<>(status, null, details);
	}
}
