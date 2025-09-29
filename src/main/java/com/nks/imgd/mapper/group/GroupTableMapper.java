package com.nks.imgd.mapper.group;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.data.GroupTableWithMstUserNameDto;
import com.nks.imgd.dto.data.GroupUserWithNameDto;

@Mapper
public interface GroupTableMapper {

	List<GroupTableWithMstUserNameDto> findGroupName(@Param("userId") String userId);

	List<GroupTableWithMstUserNameDto> findGroupWhatInside(@Param("userId") String userId);

	List<GroupUserWithNameDto> findGroupUserWhatInside(@Param("userId") String userId, @Param("groupId") Long groupId);

	GroupTableWithMstUserNameDto findGroupByGroupId(@Param("groupId") Long groupId);

	List<GroupTableWithMstUserNameDto> findGroupWhatUserIsMstAndJustOnlyOne(@Param("userId") String userId);

	int findAllGroupWhatUserMake(@Param("userId") String userId);

	int makeNewGroup(@Param("group") GroupTableWithMstUserNameDto dto);

	int makeNewGroupUserTable(@Param("group") GroupTableWithMstUserNameDto dto,
		@Param("userId") String userId);

	int isUserCheck(@Param("group") GroupTableWithMstUserNameDto dto,
		@Param("userId") String userId);

	int deleteGroupUser(@Param("group") GroupTableWithMstUserNameDto dto,
		@Param("userId") String userId);

	int changeMstUserGroup(@Param("group") GroupTableWithMstUserNameDto dto,
		@Param("userId") String userId);

	int countGroupUser(@Param("group") GroupTableWithMstUserNameDto dto);

	int deleteGroupTable(@Param("groupId") Long groupId);

}
