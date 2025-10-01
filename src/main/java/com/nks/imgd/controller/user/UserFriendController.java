package com.nks.imgd.controller.user;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.component.util.maker.ApiResponse;
import com.nks.imgd.dto.data.UserTableWithRelationshipAndPictureNmDto;
import com.nks.imgd.service.user.UserFriendService;

@RestController
@RequestMapping("/userFriend")
public class UserFriendController {

	private final UserFriendService userFriendService;
	private static final CommonMethod commonMethod = new CommonMethod();

	public UserFriendController(UserFriendService userFriendService) {
		this.userFriendService = userFriendService;
	}

	/**
	 * 친구를 추가 한다.
	 * @param jwt 로그인 한 유저 토큰 정보
	 * @param targetUserId 추가 하려는 유저 아이디
	 * @param relationship F/B/R 세가지로, 친구의 F, 블록의 B, (친구) 거부의 R이 있다.
	 * @return 상호 친구 목록
	 */
	@PostMapping()
	public ResponseEntity<ApiResponse<List<UserTableWithRelationshipAndPictureNmDto>>> insertUserFriendTable(
		@AuthenticationPrincipal Jwt jwt, @RequestParam String targetUserId, @RequestParam String relationship) {
		return commonMethod
			.responseTransaction(userFriendService.insertUserFriendTable(jwt.getSubject(), targetUserId, relationship));
	}

	/**
	 * 친구를 삭제 한다.
	 * @param jwt 로그인 한 유저 토큰 정보
	 * @param targetUserId 삭제 하려는 유저 아이디
	 * @return 상호 친구 목록
	 */
	@DeleteMapping()
	public ResponseEntity<ApiResponse<List<UserTableWithRelationshipAndPictureNmDto>>> deleteUserFriendTable(
		@AuthenticationPrincipal Jwt jwt, @RequestParam String targetUserId) {
		return commonMethod
			.responseTransaction(userFriendService.deleteUserFriendTable(jwt.getSubject(), targetUserId));
	}
}
