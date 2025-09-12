package com.nks.imgd.service.group;

import java.util.List;

import org.springframework.stereotype.Component;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.dto.dataDTO.GroupTableWithMstUserNameDTO;
import com.nks.imgd.mapper.group.GroupTableMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GroupPortAdapter implements GroupPort {

	private final GroupTableMapper groupTableMapper;
	private final CommonMethod commonMethod = new CommonMethod();

	@Override
	public List<GroupTableWithMstUserNameDTO> findGroupWhatInside(String userId) {
		return postProcessingGroupTables(groupTableMapper.findGroupWhatInside(userId));
	}

	public List<GroupTableWithMstUserNameDTO> postProcessingGroupTables(List<GroupTableWithMstUserNameDTO> groups) {

		for (GroupTableWithMstUserNameDTO group : groups) {
			postProcessingGroupTable(group);
		}

		return groups;
	}

	public void postProcessingGroupTable(GroupTableWithMstUserNameDTO group) {

		group.setRegDtm(null != group.getRegDtm() ? commonMethod.translateDate(group.getRegDtm()) : null);
		group.setModDtm(null != group.getModDtm() ? commonMethod.translateDate(group.getModDtm()) : null);
	}
}
