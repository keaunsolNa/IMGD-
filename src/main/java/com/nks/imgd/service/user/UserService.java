package com.nks.imgd.service.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nks.imgd.dto.UserTableDTO;
import com.nks.imgd.mapper.UserTableMapper;

@Service
public class UserService {

	private final UserTableMapper userTableMapper;

	public UserService(UserTableMapper userTableMapper) { this.userTableMapper = userTableMapper; }

	public List<UserTableDTO> findAllUsers() {

		return userTableMapper.findAll();
	}
}
