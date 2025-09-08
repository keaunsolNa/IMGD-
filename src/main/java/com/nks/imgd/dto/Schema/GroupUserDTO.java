package com.nks.imgd.dto.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupUserDTO {

	private Long groupId;
	private String userId;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;
}
