package com.nks.imgd.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.nks.imgd.dto.UserTableDTO;

@Mapper
public interface UserTableMapper {

	List<UserTableDTO> findAll();
}
