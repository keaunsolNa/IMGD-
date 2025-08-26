package com.nks.imgd.service.user;

import com.nks.imgd.dto.user.UserTableDTO;

public interface UserProfilePort {
	int updatePictureId(String userId, Long fileId);

	UserTableDTO findUserById(String userId);
}
