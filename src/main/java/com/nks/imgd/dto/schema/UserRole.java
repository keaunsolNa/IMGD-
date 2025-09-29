package com.nks.imgd.dto.schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

	private String userId;
	private String roleId;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;

}
