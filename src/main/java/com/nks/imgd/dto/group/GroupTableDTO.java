package com.nks.imgd.dto.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupTableDTO {

	private Long groupId;
	private String groupNm;
	private String groupMstUserId;
	private String groupMstUserNm;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;
}
