package com.nks.imgd.mapper.user;

import org.apache.ibatis.annotations.Mapper;

import com.nks.imgd.dto.schema.FriendTable;

@Mapper
public interface FriendTableMapper {

	FriendTable findFriendTableIdByUserId(String userId);

}
