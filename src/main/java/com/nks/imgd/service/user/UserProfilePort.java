package com.nks.imgd.service.user;

import com.nks.imgd.dto.userAndRole.UserTableDTO;

public interface UserProfilePort {
	int updatePictureId(String userId, Long fileId);

	UserTableDTO findUserById(String userId);
}
