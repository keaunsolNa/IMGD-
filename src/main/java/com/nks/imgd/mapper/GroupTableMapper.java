package com.nks.imgd.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.nks.imgd.dto.GroupTableDTO;

@Mapper
public interface GroupTableMapper {

	int makeGroup(GroupTableDTO groupTableDTO);
}
