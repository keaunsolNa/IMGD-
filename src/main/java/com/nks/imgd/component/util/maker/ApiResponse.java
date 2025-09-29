package com.nks.imgd.component.util.maker;

import java.time.Instant;

import com.nks.imgd.dto.enums.ResponseMsg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author nks
 * @apiNote Controller Layer 에서 ResponseEntity 반환 타입을 Enum Type ResponseMsg로 씌워서 반환한다.
 *          Service Layer 에서 ServiceResult 형식으로 반환된 타입을 responseTransaction method 를 통해 가공한다
 *          success -> true, error -> false
 *
 * @param <T> ServiceResult 결과 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

	private boolean success;
	private T data;
	private ApiError error;
	private Instant timestamp = Instant.now();

	public static <T> ApiResponse<T> ok(T data) {
		ApiResponse<T> res = new ApiResponse<>();
		res.success = true;
		res.data = data;
		res.timestamp = Instant.now();
		return res;
	}

	public static <T> ApiResponse<T> error(ResponseMsg responseMsg) {
		ApiResponse<T> res = new ApiResponse<>();
		res.success = false;
		res.error = new ApiError(responseMsg.getKey(), responseMsg.getMsg());
		res.timestamp = Instant.now();
		return res;
	}

	@Getter
	@AllArgsConstructor
	public static class ApiError {
		private String code;
		private String message;
	}

}
