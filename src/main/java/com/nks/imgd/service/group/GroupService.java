package com.nks.imgd.service.group;

import org.springframework.stereotype.Service;

import com.nks.imgd.dto.GroupTableDTO;
import com.nks.imgd.mapper.GroupTableMapper;

@Service
public class GroupService {

	private final GroupTableMapper groupTableMapper;

	public GroupService(GroupTableMapper groupTableMapper) {
		this.groupTableMapper = groupTableMapper;
	}

	public int makeGroup(GroupTableDTO groupTableDTO) {

		return groupTableMapper.makeGroup(groupTableDTO);
	}


}
