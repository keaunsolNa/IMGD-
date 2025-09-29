package com.nks.imgd.controller.user;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.component.util.maker.ApiResponse;
import com.nks.imgd.dto.data.UserTableWithRelationshipAndPictureNmDto;
import com.nks.imgd.dto.enums.ResponseMsg;
import com.nks.imgd.service.user.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

	private final UserService userService;
	private static final CommonMethod commonMethod = new CommonMethod();

	public UserController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 로그인 한 유저의 정보를 반환 한다.
	 *
	 * @param jwt 유저 토큰
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@GetMapping("/findUserByToken")
	public ResponseEntity<UserTableWithRelationshipAndPictureNmDto> findUserByToken(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(userService.findUserById(jwt.getSubject()));
	}

	/**
	 * 대상 유저의 정보를 반환 한다.
	 *
	 * @param userId 유저 아이디
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@GetMapping("/findUserById")
	public ResponseEntity<UserTableWithRelationshipAndPictureNmDto> findUserById(@RequestParam String userId) {
		return ResponseEntity.ok(userService.findUserById(userId));
	}

	/**
	 * 대상 유저가 가지고 있는 상호 친구 목록을 반환 한다.
	 * @param userId 대상 유저 아이디
	 * @return 상호 친구 목록
	 */
	@GetMapping("/findFriendEachOther")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDto>> findFriendEachOther(
		@RequestParam String userId) {
		return ResponseEntity.ok(userService.findFriendEachOther(userId));
	}

	/**
	 * 대상 유저는 친구로 등록 했지만, 본인은 등록 하지 않은 유저 목록을 반환 한다.
	 * @param userId 대상 유저 아이디
	 * @return 나를 추가한, 내가 추가 하지 않은 유저 목록
	 */
	@GetMapping("/findFriendWhoAddMeButImNot")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDto>> findFriendWhoAddMeButImNot(
		@RequestParam String userId) {
		return ResponseEntity.ok(userService.findFriendWhoAddMeButImNot(userId));
	}

	/**
	 * 내가 추가 했지만, 상대는 추가 하지 않은 친구 목록
	 * @param userId 대상 유저 아이디
	 * @return 내가 추가한, 나를 추가 하지 않은 유저 목록
	 */
	@GetMapping("/findFriendWhoImAddButNot")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDto>> findFriendWhoImAddButNot(
		@RequestParam String userId) {
		return ResponseEntity.ok(userService.findFriendWhoImAddButNot(userId));
	}

	/**
	 * 내가 추가 했지만, 상대는 거절 한 친구 목록
	 * @param userId 대상 유저 아이디
	 * @return 내가 추가한, 나를 거절 한 않은 유저 목록
	 */
	@GetMapping("/findFriendWhoImAddButReject")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDto>> findFriendWhoImAddButReject(
		@RequestParam String userId) {
		return ResponseEntity.ok(userService.findFriendWhoImAddButReject(userId));
	}

	/**
	 * 전체 친구 목록을 반환 한다.
	 * @param userId 대상 유저 아이디
	 * @return 내가 추가한 모든 친구 유저 목록
	 */
	@GetMapping("/findFriend")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDto>> findFriend(@RequestParam String userId) {
		return ResponseEntity.ok(userService.findFriend(userId));
	}

	/**
	 * 대상과 나와의 관계를 검색한다.
	 * @param userId 대상 유저 아이디
	 * @return 내가 추가한 모든 친구 유저 목록
	 */
	@GetMapping("/searchFriend")
	public ResponseEntity<UserTableWithRelationshipAndPictureNmDto> searchFriend(@AuthenticationPrincipal Jwt jwt,
		@RequestParam String userId) {
		return ResponseEntity.ok(userService.searchFriend(jwt.getSubject(), userId));
	}

	/**
	 * 그룹에 소속 되지 않은 친구 목록을 반환 한다.
	 * @param userId 대상 유저 아이디
	 * @param groupId 대상 그룹 아이디
	 * @return 그룹에 소속 되지 않은 친구 목록
	 */
	@GetMapping("/findFriendEachOtherAndNotInGroup")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDto>> findFriendEachOtherAndNotInGroup(
		@RequestParam String userId, @RequestParam Long groupId) {
		return ResponseEntity.ok(userService.findFriendEachOtherAndNotInGroup(userId, groupId));
	}

	/**
	 * 대상 유저의 정보를 변경 한다.
	 *
	 * @param user 유저 정보
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@PostMapping("/updateUser")
	public ResponseEntity<ApiResponse<UserTableWithRelationshipAndPictureNmDto>> updateUser(
		@RequestBody UserTableWithRelationshipAndPictureNmDto user) {
		return commonMethod.responseTransaction(userService.updateUser(user));
	}

	/**
	 * 유저 정보를 삭제한다.
	 * 이 때 유저가 GROUP_MST_USER 로 있는 그룹이 있을 경우, 해당 유저만 있다면 그룹을 삭제한다.
	 * 해당 인원 외의 인원이 있다면 가장 오래된 가입자를 GROUP_MST_USER로 변경한다. (TRIGGER, T_DELETE_USER)
	 *
	 * @param userId 대상 유저 아이디
	 * @return 실행 결과
	 */
	@DeleteMapping("/deleteUser")
	public ResponseEntity<ApiResponse<ResponseMsg>> deleteUser(@RequestParam String userId) {
		return commonMethod.responseTransaction(userService.deleteUser(userId));
	}

}
