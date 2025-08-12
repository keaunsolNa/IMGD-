package com.nks.imgd.controller.user;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nks.imgd.dto.userAndRole.UserTableDTO;
import com.nks.imgd.service.user.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) { this.userService = userService; }

	@GetMapping
	public List<UserTableDTO> findAll() { return userService.findAllUsers();}
}
