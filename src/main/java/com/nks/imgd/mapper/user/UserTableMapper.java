package com.nks.imgd.mapper.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.nks.imgd.dto.userAndRole.UserTableDTO;

@Mapper
public interface UserTableMapper {

	List<UserTableDTO> findAll();
}
