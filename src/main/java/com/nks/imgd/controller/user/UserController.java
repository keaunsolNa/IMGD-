package com.nks.imgd.controller.user;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	@GetMapping()
	public ResponseEntity<UserTableWithRelationshipAndPictureNmDto> findUserByToken(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(userService.findUserById(jwt.getSubject()));
	}

	/**
	 * 대상 유저의 정보를 반환 한다.
	 *
	 * @param userId 유저 아이디
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@GetMapping("/{userId}")
	public ResponseEntity<UserTableWithRelationshipAndPictureNmDto> findUserById(@PathVariable String userId) {
		return ResponseEntity.ok(userService.findUserById(userId));
	}

	/**
	 * 친구 목록을 반환한다.
	 * option 에 따라 반환하는 형태가 다르다.
	 * option = 1 : 대상 유저가 가지고 있는 상호 친구 목록을 반환 한다.
	 * option = 2 : 대상 유저는 친구로 등록 했지만, 본인은 등록 하지 않은 유저 목록을 반환 한다.
	 * option = 3 : 내가 추가 했지만, 상대는 추가 하지 않은 친구 목록
	 * option = 4 : 내가 추가 했지만, 상대는 거절 한 친구 목록
	 * option = 5 : 전체 친구 목록을 반환 한다.
	 * @param userId 대상 유저 아이디
	 * @return 상호 친구 목록
	 */
	@GetMapping("/{userId}/friends")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDto>> findFriendEachOther(
		@PathVariable String userId, @RequestParam int option) {

		return switch (option) {
			case 1 -> ResponseEntity.ok(userService.findFriendEachOther(userId));
			case 2 -> ResponseEntity.ok(userService.findFriendWhoAddMeButImNot(userId));
			case 3 -> ResponseEntity.ok(userService.findFriendWhoImAddButNot(userId));
			case 4 -> ResponseEntity.ok(userService.findFriendWhoImAddButReject(userId));
			case 5 -> ResponseEntity.ok(userService.findFriend(userId));
			default -> ResponseEntity.badRequest().body(null);
		};

	}

	/**
	 * 대상과 나와의 관계를 검색한다.
	 * @param userId 대상 유저 아이디
	 * @return 내가 추가한 모든 친구 유저 목록
	 */
	@GetMapping("/relationship")
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
	@GetMapping("/group")
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
	@PostMapping()
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
	@DeleteMapping()
	public ResponseEntity<ApiResponse<ResponseMsg>> deleteUser(@RequestParam String userId) {
		return commonMethod.responseTransaction(userService.deleteUser(userId));
	}

}
