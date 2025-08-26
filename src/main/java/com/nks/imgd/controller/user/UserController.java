package com.nks.imgd.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.nks.imgd.dto.user.UserTableDTO;
import com.nks.imgd.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) { this.userService = userService; }

	/**
	 * 로그인 한 유저의 정보를 반환 한다.
	 *
	 * @param jwt 유저 토큰
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@GetMapping("/findUserByToken")
	public ResponseEntity<UserTableDTO> findUserByToken(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(userService.findUserById(jwt.getSubject()));
	}

	/**
	 * 대상 유저의 정보를 반환 한다.
	 *
	 * @param userId 유저 아이디
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@GetMapping("/findUserById")
	public ResponseEntity<UserTableDTO> findUserById(@RequestParam String userId) {
		return ResponseEntity.ok(userService.findUserById(userId));
	}

	/**
	 * 대상 유저가 가지고 있는 상호 친구 목록을 반환 한다.
	 * @param userId 대상 유저 아이디
	 * @return 상호 친구 목록
	 */
	@GetMapping("/findFriendEachOther")
	public ResponseEntity<List<UserTableDTO>> findFriendEachOther(@RequestParam String userId) {
		return ResponseEntity.ok(userService.findFriendEachOther(userId));
	}

	/**
	 * 대상 유저를 친구로 등록 했지만, 본인은 등록 하지 않은 유저 목록을 반환 한다.
	 * @param userId 대상 유저 아이디
	 * @return 나를 추가한, 내가 추가 하지 않은 유저 목록
	 */
	@GetMapping("/findFriendWhoAddMeButImNot")
	public ResponseEntity<List<UserTableDTO>> findFriendWhoAddMeButImNot(@RequestParam String userId) {
		return ResponseEntity.ok(userService.findFriendWhoAddMeButImNot(userId));
	}

	/**
	 * 대상 유저의 정보를 변경 한다.
	 *
	 * @param user 유저 정보
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@PostMapping("/updateUser")
	public ResponseEntity<UserTableDTO> updateUser(@RequestBody UserTableDTO user) {
		return userService.updateUser(user);
	}

	/**
	 * 친구를 추가 한다.
	 * @param jwt 로그인 한 유저 토큰 정보
	 * @param targetUserId 추가 하려는 유저 아이디
	 * @return 상호 친구 목록
	 */
	@PostMapping("/insertUserFriendTable")
	public ResponseEntity<List<UserTableDTO>> insertUserFriendTable(@AuthenticationPrincipal Jwt jwt, String targetUserId) {
		return userService.insertUserFriendTable(jwt.getSubject(), targetUserId);
	}

	/**
	 * 친구를 삭제 한다.
	 * @param jwt 로그인 한 유저 토큰 정보
	 * @param targetUserId 삭제 하려는 유저 아이디
	 * @return 상호 친구 목록
	 */
	@DeleteMapping("/deleteUserFriendTable")
	public ResponseEntity<List<UserTableDTO>> deleteUserFriendTable(@AuthenticationPrincipal Jwt jwt, String targetUserId) {
		return userService.deleteUserFriendTable(jwt.getSubject(), targetUserId);
	}
}
