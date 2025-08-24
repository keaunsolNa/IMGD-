package com.nks.imgd.mapper.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.userAndRole.RolesDTO;
import com.nks.imgd.dto.userAndRole.UserTableDTO;

@Mapper
public interface UserTableMapper {

	UserTableDTO findById(String id);

	RolesDTO findHighestUserRole(String id);

	void makeNewUser(@Param("user") UserTableDTO user);

	int updateUser(@Param("user") UserTableDTO user);

	int updatePictureId(@Param("userId") String userId,
						@Param("fileId") Long fileId);

}
