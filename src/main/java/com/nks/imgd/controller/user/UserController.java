package com.nks.imgd.controller.user;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.component.util.maker.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.nks.imgd.dto.dataDTO.UserTableWithRelationshipAndPictureNmDTO;
import com.nks.imgd.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

	private final UserService userService;
    private static final CommonMethod commonMethod = new CommonMethod();

	public UserController(UserService userService) { this.userService = userService; }

	/**
	 * 로그인 한 유저의 정보를 반환 한다.
	 *
	 * @param jwt 유저 토큰
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@GetMapping("/findUserByToken")
	public ResponseEntity<UserTableWithRelationshipAndPictureNmDTO> findUserByToken(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(userService.findUserById(jwt.getSubject()));
	}

	/**
	 * 대상 유저의 정보를 반환 한다.
	 *
	 * @param userId 유저 아이디
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@GetMapping("/findUserById")
	public ResponseEntity<UserTableWithRelationshipAndPictureNmDTO> findUserById(@RequestParam String userId) {
		return ResponseEntity.ok(userService.findUserById(userId));
	}

	/**
	 * 대상 유저가 가지고 있는 상호 친구 목록을 반환 한다.
	 * @param userId 대상 유저 아이디
	 * @return 상호 친구 목록
	 */
	@GetMapping("/findFriendEachOther")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDTO>> findFriendEachOther(@RequestParam String userId) {
		return ResponseEntity.ok(userService.findFriendEachOther(userId));
	}

	/**
	 * 대상 유저는 친구로 등록 했지만, 본인은 등록 하지 않은 유저 목록을 반환 한다.
	 * @param userId 대상 유저 아이디
	 * @return 나를 추가한, 내가 추가 하지 않은 유저 목록
	 */
	@GetMapping("/findFriendWhoAddMeButImNot")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDTO>> findFriendWhoAddMeButImNot(@RequestParam String userId) {
		return ResponseEntity.ok(userService.findFriendWhoAddMeButImNot(userId));
	}

	/**
	 * 내가 추가 했지만, 상대는 추가 하지 않은 친구 목록
	 * @param userId 대상 유저 아이디
	 * @return 내가 추가한, 나를 추가 하지 않은 유저 목록
	 */
	@GetMapping("/findFriendWhoImAddButNot")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDTO>> findFriendWhoImAddButNot(@RequestParam String userId) {
		return ResponseEntity.ok(userService.findFriendWhoImAddButNot(userId));
	}

	/**
	 * 내가 추가 했지만, 상대는 거절 한 친구 목록
	 * @param userId 대상 유저 아이디
	 * @return 내가 추가한, 나를 거절 한 않은 유저 목록
	 */
	@GetMapping("/findFriendWhoImAddButReject")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDTO>> findFriendWhoImAddButReject(@RequestParam String userId) {
		return ResponseEntity.ok(userService.findFriendWhoImAddButReject(userId));
	}

	/**
	 * 전체 친구 목록을 반환 한다.
	 * @param userId 대상 유저 아이디
	 * @return 내가 추가한 모든 친구 유저 목록
	 */
	@GetMapping("/findFriend")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDTO>> findFriend(@RequestParam String userId) {
		return ResponseEntity.ok(userService.findFriend(userId));
	}

	/**
	 * 대상과 나와의 관계를 검색한다.
	 * @param userId 대상 유저 아이디
	 * @return 내가 추가한 모든 친구 유저 목록
	 */
	@GetMapping("/searchFriend")
	public ResponseEntity<UserTableWithRelationshipAndPictureNmDTO> searchFriend(@AuthenticationPrincipal Jwt jwt, @RequestParam String userId) {
 		return ResponseEntity.ok(userService.searchFriend(jwt.getSubject(), userId));
	}

	/**
	 * 그룹에 소속 되지 않은 친구 목록을 반환 한다.
	 * @param userId 대상 유저 아이디
	 * @param groupId 대상 그룹 아이디
	 * @return 그룹에 소속 되지 않은 친구 목록
	 */
	@GetMapping("/findFriendEachOtherAndNotInGroup")
	public ResponseEntity<List<UserTableWithRelationshipAndPictureNmDTO>> findFriendEachOtherAndNotInGroup(@RequestParam String userId, @RequestParam Long groupId) {
		return ResponseEntity.ok(userService.findFriendEachOtherAndNotInGroup(userId, groupId));
	}
	
	/**
	 * 대상 유저의 정보를 변경 한다.
	 *
	 * @param user 유저 정보
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@PostMapping("/updateUser")
	public ResponseEntity<ApiResponse<UserTableWithRelationshipAndPictureNmDTO>> updateUser(@RequestBody UserTableWithRelationshipAndPictureNmDTO user) {
		return commonMethod.responseTransaction(userService.updateUser(user));
	}

	/**
	 * 친구를 추가 한다.
	 * @param jwt 로그인 한 유저 토큰 정보
	 * @param targetUserId 추가 하려는 유저 아이디
	 * @param relationship F/B/R 세가지로, 친구의 F, 블록의 B, (친구) 거부의 R이 있다.
	 * @return 상호 친구 목록
	 */
	@PostMapping("/insertUserFriendTable")
	public ResponseEntity<ApiResponse<List<UserTableWithRelationshipAndPictureNmDTO>>> insertUserFriendTable(@AuthenticationPrincipal Jwt jwt, @RequestParam String targetUserId, @RequestParam String relationship) {
		return commonMethod.responseTransaction(userService.insertUserFriendTable(jwt.getSubject(), targetUserId, relationship));
	}

	/**
	 * 친구를 삭제 한다.
	 * @param jwt 로그인 한 유저 토큰 정보
	 * @param targetUserId 삭제 하려는 유저 아이디
	 * @return 상호 친구 목록
	 */
	@DeleteMapping("/deleteUserFriendTable")
	public ResponseEntity<ApiResponse<List<UserTableWithRelationshipAndPictureNmDTO>>> deleteUserFriendTable(@AuthenticationPrincipal Jwt jwt, @RequestParam String targetUserId) {
		return commonMethod.responseTransaction(userService.deleteUserFriendTable(jwt.getSubject(), targetUserId));
	}

}
