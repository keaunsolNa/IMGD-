package com.nks.imgd.dto.schema;

import com.nks.imgd.dto.enums.SocialLoginType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTable {

	private String userId;
	private String name;
	private String email;
	private String nickName;
	private Long pictureId;
	private SocialLoginType loginType;
	private String lastLoginDate;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;

}
