package com.nks.imgd.mapper.user;

import com.nks.imgd.dto.user.FriendTableDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.Role.RolesDTO;
import com.nks.imgd.dto.user.UserTableDTO;

import java.util.List;

@Mapper
public interface UserTableMapper {

	UserTableDTO findById(String userId);

	RolesDTO findHighestUserRole(String userId);

	List<UserTableDTO> findFriendEachOther(String userId);

	List<UserTableDTO> findFriendWhoAddMeButImNot(String userId);

	FriendTableDTO findFriendTableIdByUserId(String userId);

	void makeNewUser(@Param("user") UserTableDTO user);

	int updateUser(@Param("user") UserTableDTO user);

	int updatePictureId(@Param("userId") String userId,
						@Param("fileId") Long fileId);

	int insertUserFriendTable(@Param("userId") String userId,
							  @Param("friendId") Long friendId);

	int deleteUserFriendTable(@Param("userId") String userId,
							  @Param("friendId") Long friendId);
}
