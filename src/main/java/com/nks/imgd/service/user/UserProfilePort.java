package com.nks.imgd.service.user;

import com.nks.imgd.dto.Schema.RolesDTO;
import com.nks.imgd.dto.dataDTO.UserTableWithRelationshipAndPictureNmDTO;

public interface UserProfilePort {
	int updatePictureId(String userId, Long fileId);

	UserTableWithRelationshipAndPictureNmDTO findUserById(String userId);

	RolesDTO findHighestUserRole(String userId);
}
