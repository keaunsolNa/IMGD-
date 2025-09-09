package com.nks.imgd.dto.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum ResponseMsg {

    NOT_FOUND("NOT_FOUND", "요청하신 리소스를 찾을 수 없습니다.", null),
    BAD_REQUEST("BAD_REQUEST", "잘못 된 요청입니다.", null),
    ON_SUCCESS("ON_SUCCESS", "요청이 성공했습니다", null),
    GROUP_LIMIT_EXCEEDED("GROUP_LIMIT_EXCEEDED", "그룹 생성 한도를 초과했습니다.", null),
    GROUP_CREATE_FAILED("GROUP_CREATE_FAILED", "그룹 생성에 실패했습니다.", null);

    private final String key;
    private final String msg;
    private final Map<String, Object> details;

}
