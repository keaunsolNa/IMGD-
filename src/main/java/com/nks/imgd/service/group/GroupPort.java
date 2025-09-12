package com.nks.imgd.service.group;

import java.util.List;

import com.nks.imgd.dto.dataDTO.GroupTableWithMstUserNameDTO;

public interface GroupPort {

	List<GroupTableWithMstUserNameDTO> findGroupWhatInside(String userId);

}
