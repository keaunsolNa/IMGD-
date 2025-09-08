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

	List<UserTableDTO> findFriendWhoImAddButReject(String userId);

	FriendTableDTO findFriendTableIdByUserId(String userId);

	List<UserTableDTO> findFriendWhoImAddButNot(String userId);

	List<UserTableDTO> findFriend(String userId);

	UserTableDTO searchFriend(@Param("loginId") String loginId, @Param("userId") String userId);

	List<UserTableDTO> findFriendEachOtherAndNotInGroup(@Param("userId") String userId, @Param("groupId") Long groupId);

	void makeNewUser(@Param("user") UserTableDTO user);

	int updateUser(@Param("user") UserTableDTO user);

	int updatePictureId(@Param("userId") String userId,
						@Param("fileId") Long fileId);

	int insertUserFriendTable(@Param("targetUserId") String targetUserId,
							  @Param("friendId") Long friendId,
							  @Param("userId") String userId,
							  @Param("relationship") String relationship);

	int deleteUserFriendTable(@Param("targetUserId") String targetUserId,
							  @Param("friendId") Long friendId);
}
