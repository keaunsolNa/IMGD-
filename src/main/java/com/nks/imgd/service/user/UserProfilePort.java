package com.nks.imgd.service.user;

import com.nks.imgd.dto.data.UserTableWithRelationshipAndPictureNmDto;
import com.nks.imgd.dto.schema.Roles;

public interface UserProfilePort {
	int updatePictureId(String userId, Long fileId);

	UserTableWithRelationshipAndPictureNmDto findUserById(String userId);

	Roles findHighestUserRole(String userId);
}
