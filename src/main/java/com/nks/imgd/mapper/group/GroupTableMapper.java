package com.nks.imgd.mapper.group;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.nks.imgd.dto.dataDTO.GroupTableWithMstUserNameDTO;
import com.nks.imgd.dto.dataDTO.GroupUserWithNameDTO;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface GroupTableMapper {

	List<GroupTableWithMstUserNameDTO> findGroupName(@Param("userId") String userId);

	List<GroupTableWithMstUserNameDTO> findGroupWhatInside(@Param("userId") String userId);

	List<GroupUserWithNameDTO> findGroupUserWhatInside(@Param("userId") String userId, @Param("groupId") Long groupId);

	GroupTableWithMstUserNameDTO findGroupByGroupId(@Param("groupId") Long groupId);

    int findAllGroupWhatUserMake(@Param("userId") String userId);

	int makeNewGroup(@Param("group") GroupTableWithMstUserNameDTO dto);

	int makeNewGroupUserTable(@Param("group") GroupTableWithMstUserNameDTO dto,
							  @Param("userId") String userId);

	int isUserCheck(@Param("group") GroupTableWithMstUserNameDTO dto,
					   @Param("userId") String userId);

	int deleteGroupUser(@Param("group") GroupTableWithMstUserNameDTO dto,
					@Param("userId") String userId);

	int changeMstUserGroup(@Param("group") GroupTableWithMstUserNameDTO dto,
					@Param("userId") String userId);

	int countGroupUser(@Param("group") GroupTableWithMstUserNameDTO dto);

	int deleteGroupTable(@Param("groupId") Long groupId);

}
