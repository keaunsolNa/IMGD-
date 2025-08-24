package com.nks.imgd.service.user;

import org.springframework.stereotype.Component;

import com.nks.imgd.dto.userAndRole.UserTableDTO;
import com.nks.imgd.mapper.user.UserTableMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserProfilePortAdapter implements UserProfilePort {

	private final UserTableMapper userTableMapper;

	@Override
	public int updatePictureId(String userId, Long fileId) {
		return userTableMapper.updatePictureId(userId, fileId);
	}

	@Override
	public UserTableDTO findUserById(String userId) {
		return userTableMapper.findById(userId);
	}
}
