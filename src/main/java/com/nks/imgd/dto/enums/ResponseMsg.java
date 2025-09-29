package com.nks.imgd.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMsg {

	NOT_FOUND("NOT_FOUND", "요청하신 리소스를 찾을 수 없습니다."),
	BAD_REQUEST("BAD_REQUEST", "잘못 된 요청입니다."),
	ON_SUCCESS("ON_SUCCESS", "요청이 성공했습니다"),
	CAN_NOT_FIND_USER("CAN_NOT_FIND_USER", "유저 정보가 없습니다"),
	GROUP_LIMIT_EXCEEDED("GROUP_LIMIT_EXCEEDED", "그룹 생성 한도를 초과했습니다."),
	GROUP_CREATE_FAILED("GROUP_CREATE_FAILED", "그룹 생성에 실패했습니다."),
	GROUP_MST_USER_CANT_DELETE("GROUP_MST_USER_CANT_DELETE", "마스터 유저는 그룹에서 제거할 수 없습니다. \n 그룹 마스터 권한을 다른 유저에게 넘기세요"),
	ALREADY_JOIN_USER("ALERT_JOIN_USER", "이미 가입된 인원입니다."),
	FILE_CREATE_FAILED("FILE_CREATE_FAILED", "파일 생성에 실패했습니다."),
	FILE_DELETE_FAILED("FILE_DELETE_FAILED", "파일 삭제에 실패했습니다."),
	FILE_UPDATE_FAILED("FILE_UPDATE_FAILED", "파일 변경에 실패했습니다.");

	private final String key;
	private final String msg;

}
