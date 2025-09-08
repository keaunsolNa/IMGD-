package com.nks.imgd.dto.dataDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupUserWithNameDTO {

	private Long groupId;
	private String userId;
	private String userNm;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;
}
