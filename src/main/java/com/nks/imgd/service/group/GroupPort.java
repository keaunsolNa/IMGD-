package com.nks.imgd.service.group;

import java.util.List;

import com.nks.imgd.dto.data.GroupTableWithMstUserNameDto;

public interface GroupPort {

	List<GroupTableWithMstUserNameDto> findGroupWhatInside(String userId);

}
