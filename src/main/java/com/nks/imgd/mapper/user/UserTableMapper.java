package com.nks.imgd.mapper.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.userAndRole.RolesDTO;
import com.nks.imgd.dto.userAndRole.UserTableDTO;

@Mapper
public interface UserTableMapper {

	List<UserTableDTO> findAll();

	UserTableDTO findById(String id);

	RolesDTO findHighestUserRole(String id);

	void makeNewUser(@Param("user") UserTableDTO user);

	void updateUser(@Param("user") UserTableDTO user);
}
