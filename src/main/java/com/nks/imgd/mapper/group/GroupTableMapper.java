package com.nks.imgd.mapper.group;

import org.apache.ibatis.annotations.Mapper;

import com.nks.imgd.dto.group.GroupTableDTO;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GroupTableMapper {

	int makeNewGroup(@Param("group") GroupTableDTO dto);

	int makeNewGroupUserTable(@Param("group") GroupTableDTO dto,
							  @Param("userId") String userId);

	int isUserCheck(@Param("group") GroupTableDTO dto,
					   @Param("userId") String userId);

	int deleteGroupUser(@Param("group") GroupTableDTO dto,
					@Param("userId") String userId);

	int changeMstUserGroup(@Param("group") GroupTableDTO dto,
					@Param("userId") String userId);

	int countGroupUser(@Param("group") GroupTableDTO dto);

	int deleteGroupTable(@Param("group") GroupTableDTO dto);

}
