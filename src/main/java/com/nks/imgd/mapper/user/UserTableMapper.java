package com.nks.imgd.mapper.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.data.UserTableWithRelationshipAndPictureNmDto;
import com.nks.imgd.dto.schema.Roles;

@Mapper
public interface UserTableMapper {

	UserTableWithRelationshipAndPictureNmDto findById(String userId);

	Roles findHighestUserRole(String userId);

	List<UserTableWithRelationshipAndPictureNmDto> findFriendEachOther(String userId);

	List<UserTableWithRelationshipAndPictureNmDto> findFriendWhoAddMeButImNot(String userId);

	List<UserTableWithRelationshipAndPictureNmDto> findFriendWhoImAddButReject(String userId);

	List<UserTableWithRelationshipAndPictureNmDto> findFriendWhoImAddButNot(String userId);

	List<UserTableWithRelationshipAndPictureNmDto> findFriend(String userId);

	UserTableWithRelationshipAndPictureNmDto searchFriend(@Param("loginId") String loginId,
		@Param("userId") String userId);

	List<UserTableWithRelationshipAndPictureNmDto> findFriendEachOtherAndNotInGroup(@Param("userId") String userId,
		@Param("groupId") Long groupId);

	void makeNewUser(@Param("user") UserTableWithRelationshipAndPictureNmDto user);

	int updateUser(@Param("user") UserTableWithRelationshipAndPictureNmDto user);

	int updatePictureId(@Param("userId") String userId,
		@Param("fileId") Long fileId);

	int deleteUser(@Param("userId") String userId);
}
