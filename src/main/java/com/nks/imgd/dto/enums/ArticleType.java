package com.nks.imgd.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ArticleType {

	POST("POST", "post"),
	COMMENT("COMMENT", "comment");

	private final String key;
	private final String value;
}
