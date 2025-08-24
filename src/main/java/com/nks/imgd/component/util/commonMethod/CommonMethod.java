package com.nks.imgd.component.util.commonMethod;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

import org.springframework.stereotype.Component;

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
}
