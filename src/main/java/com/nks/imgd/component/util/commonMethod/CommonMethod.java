package com.nks.imgd.component.util.commonMethod;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

import com.nks.imgd.component.util.maker.ApiResponse;
import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.Enum.ResponseMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static com.nks.imgd.dto.Enum.ResponseMsg.*;
import static com.nks.imgd.dto.Enum.ResponseMsg.GROUP_CREATE_FAILED;
import static com.nks.imgd.dto.Enum.ResponseMsg.GROUP_LIMIT_EXCEEDED;

/**
 * @author nks
 * @apiNote 공용으로 사용할 함수를 저장하는 클래스
 */
@Component
public class CommonMethod {

	private static final DateTimeFormatter IN =
		DateTimeFormatter.ofPattern("uuuuMMdd")
			.withResolverStyle(ResolverStyle.STRICT);

	private static final DateTimeFormatter OUT =
		DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREAN);

    private static final Logger CONTROLLER_LOG = LoggerFactory.getLogger("CONTROLLER_LOG");

	/**
	 * YYYYMMDD 형태로 되어 있는 DB 데이트를 YYYY년 MM월 DD일 형태로 변환한다.
	 * 명명규칙 : [형용사] [동물이름] [3자리숫자]
	 */
	public String translateDate(String date)
	{
		if (date == null)
		{
			return "";
		}

		else if (!date.matches("\\d{8}")) {
			return "입력은 8자리 숫자(yyyyMMdd)여야 합니다: " + date;
		}

		LocalDate localDate = LocalDate.parse(date, IN);
		return localDate.format(OUT);
	}

    /**
     * Transaction 결과 값을 반환 한다.
     * Service Layer 의 비지니스 결과값을 ResponseEntity 형식으로 반환한다.
     *
     * @param result 결과값
     * @return 결과값
     */
    public <T> ResponseEntity<ApiResponse<T>> responseTransaction(ServiceResult<T> result) {

        ResponseMsg status = result.status();
        CONTROLLER_LOG.info("result, {}", status);

        if (status.equals(ON_SUCCESS)) {
            T payload = result.onSuccess().get();
            CONTROLLER_LOG.info("payload, {}", payload);
            return ResponseEntity.ok(ApiResponse.ok(payload));
        }
        else if (status.equals(NOT_FOUND)) return ResponseEntity.status(404).body(ApiResponse.error(NOT_FOUND));
        else if (status.equals(GROUP_LIMIT_EXCEEDED)) return ResponseEntity.status(400).body(ApiResponse.error(GROUP_LIMIT_EXCEEDED));
        else if (status.equals(GROUP_CREATE_FAILED)) return ResponseEntity.status(400).body(ApiResponse.error(GROUP_CREATE_FAILED));
        else return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMsg.BAD_REQUEST));
    }


    /**
     * 결과 값을 ResponseMsg 형태로 반환 한다.
     *
     * @param result DB 결과 값
     * @return ResponseMsg
     */
    public ResponseMsg returnResultByResponseMsg(int result)
    {
        if (result == 1) return ResponseMsg.ON_SUCCESS;
        else if (result == 0) return ResponseMsg.NOT_FOUND;
        else return ResponseMsg.BAD_REQUEST;
    }
}
