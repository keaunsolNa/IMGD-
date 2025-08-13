package com.nks.imgd.dto.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote User 권한 관리용 ENUM
 */
@Getter
@RequiredArgsConstructor
public enum Role {

	GUEST("ADMIN", "Administrator"),
	USER("USER", "NormalUser"),
	ADMIN("USER_NO_ADS", "User Who No Ads"),
	INVALID("USER_PAID", "User Who Paid");

	private final String key;
	private final String title;
}