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

    USER("USER", "NormalUser", 3, new int[] {25, 4, 7}),
    USER_NO_ADS("USER_NO_ADS", "User Who No Ads", 3, new int[] {50, 4, 7}),
    USER_PAID("USER_PAID", "User Who Paid", 5, new int[] {75, 4, 7}),
    ADMIN("ADMIN", "Administrator", 99, new int[] { 75, 4, 7});

	private final String key;
	private final String title;
    private final int permissionOfMakeGroup;
    private final int[] permissionOfWebpWriter;
}