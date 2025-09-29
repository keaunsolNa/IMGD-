package com.nks.imgd.mapper.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.data.UserTableWithRelationshipAndPictureNmDto;

@Mapper
public interface UserFriendTableMapper {

	List<UserTableWithRelationshipAndPictureNmDto> findFriendEachOther(String userId);

	int insertUserFriendTable(@Param("targetUserId") String targetUserId,
		@Param("friendId") Long friendId,
		@Param("userId") String userId,
		@Param("relationship") String relationship);

	int deleteUserFriendTable(@Param("targetUserId") String targetUserId,
		@Param("friendId") Long friendId);

}
