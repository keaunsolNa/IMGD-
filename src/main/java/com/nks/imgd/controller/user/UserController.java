package com.nks.imgd.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nks.imgd.dto.userAndRole.UserTableDTO;
import com.nks.imgd.service.user.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) { this.userService = userService; }

	/**
	 * 로그인한 유저의 정보를 가져온다.
	 *
	 * @param jwt 유저 토큰
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@GetMapping("/findUserByToken")
	public ResponseEntity<UserTableDTO> findUserByToken(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(userService.findUserById(jwt.getSubject()));
	}

	/**
	 * 대상 유저의 정보를 가져온다.
	 *
	 * @param userId 유저 아이디
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@GetMapping("/findUserById")
	public ResponseEntity<UserTableDTO> findUserById(@RequestParam String userId) {
		return ResponseEntity.ok(userService.findUserById(userId));
	}

	/**
	 * 대상 유저의 정보를 변경한다.
	 *
	 * @param user 유저 정보
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@PostMapping("/updateUser")
	public ResponseEntity<UserTableDTO> updateUser(@RequestBody UserTableDTO user) {

		int result = userService.updateUser(user);

		if (result == 0)
		{
			return ResponseEntity.notFound().build();
		}

		else if (result == 1)
		{
			return ResponseEntity.ok(userService.findUserById(user.getUserId()));
		}

		else
		{
			return ResponseEntity.badRequest().build();
		}
	}
}
