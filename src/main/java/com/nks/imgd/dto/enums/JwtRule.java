package com.nks.imgd.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote JWT TOKEN 권한 관리 위한 ENUM
 */
@RequiredArgsConstructor
@Getter
public enum JwtRule {

	TYPE("TYPE", "type"),
	RESOURCE_ACCESS("RESOURCE_ACCESS", "resource_access"),
	ACCOUNT("ACCOUNT", "account"),
	ROLES("ROLES", "roles"),
	ROLE_PREFIX("ROLE_PREFIX", "ROLE_");

	private final String key;
	private final String value;
}
