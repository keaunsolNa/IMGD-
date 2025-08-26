package com.nks.imgd.dto.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleDTO {

	private String userId;
	private String roleId;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;

}
