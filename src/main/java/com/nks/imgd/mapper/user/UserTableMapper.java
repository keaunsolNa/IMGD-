package com.nks.imgd.mapper.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.Schema.Roles;
import com.nks.imgd.dto.dataDTO.UserTableWithRelationshipAndPictureNmDTO;

import java.util.List;

@Mapper
public interface UserTableMapper {

	UserTableWithRelationshipAndPictureNmDTO findById(String userId);

	Roles findHighestUserRole(String userId);

	List<UserTableWithRelationshipAndPictureNmDTO> findFriendEachOther(String userId);

	List<UserTableWithRelationshipAndPictureNmDTO> findFriendWhoAddMeButImNot(String userId);

	List<UserTableWithRelationshipAndPictureNmDTO> findFriendWhoImAddButReject(String userId);

	List<UserTableWithRelationshipAndPictureNmDTO> findFriendWhoImAddButNot(String userId);

	List<UserTableWithRelationshipAndPictureNmDTO> findFriend(String userId);

	UserTableWithRelationshipAndPictureNmDTO searchFriend(@Param("loginId") String loginId, @Param("userId") String userId);

	List<UserTableWithRelationshipAndPictureNmDTO> findFriendEachOtherAndNotInGroup(@Param("userId") String userId, @Param("groupId") Long groupId);

	void makeNewUser(@Param("user") UserTableWithRelationshipAndPictureNmDTO user);

	int updateUser(@Param("user") UserTableWithRelationshipAndPictureNmDTO user);

	int updatePictureId(@Param("userId") String userId,
						@Param("fileId") Long fileId);

	int deleteUser(@Param("userId") String userId);
}
