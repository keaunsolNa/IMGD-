package com.nks.imgd.service.group;

import java.util.List;

import org.springframework.stereotype.Component;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.dto.data.GroupTableWithMstUserNameDto;
import com.nks.imgd.mapper.group.GroupTableMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GroupPortAdapter implements GroupPort {

	private final GroupTableMapper groupTableMapper;
	private final CommonMethod commonMethod = new CommonMethod();

	@Override
	public List<GroupTableWithMstUserNameDto> findGroupWhatInside(String userId) {
		return postProcessingGroupTables(groupTableMapper.findGroupWhatInside(userId));
	}

	public List<GroupTableWithMstUserNameDto> postProcessingGroupTables(List<GroupTableWithMstUserNameDto> groups) {

		for (GroupTableWithMstUserNameDto group : groups) {
			postProcessingGroupTable(group);
		}

		return groups;
	}

	public void postProcessingGroupTable(GroupTableWithMstUserNameDto group) {

		group.setRegDtm(null != group.getRegDtm() ? commonMethod.translateDate(group.getRegDtm()) : null);
		group.setModDtm(null != group.getModDtm() ? commonMethod.translateDate(group.getModDtm()) : null);
	}
}
