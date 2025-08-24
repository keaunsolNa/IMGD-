package com.nks.imgd.dto.userAndRole;

import com.nks.imgd.dto.Enum.SocialLoginType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTableDTO {

	private String userId;
	private String name;
	private String email;
	private String nickName;
	private Long pictureId;
	private String pictureUrl;
	private SocialLoginType loginType;
	private String lastLoginDate;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;

}
