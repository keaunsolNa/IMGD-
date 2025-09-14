package com.nks.imgd.service.user;

import org.springframework.stereotype.Component;

import com.nks.imgd.dto.Schema.Roles;
import com.nks.imgd.dto.dataDTO.UserTableWithRelationshipAndPictureNmDTO;
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
	public UserTableWithRelationshipAndPictureNmDTO findUserById(String userId) {
		return userTableMapper.findById(userId);
	}

	@Override
	public Roles findHighestUserRole(String userId) {
		return userTableMapper.findHighestUserRole(userId);
	}
}
