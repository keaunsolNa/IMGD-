package com.nks.imgd.dto.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolesDTO {

	private Integer roleId;
	private String roleNm;
	private String roleDesc;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;
}
