package com.nks.imgd.dto.data;

import com.nks.imgd.dto.enums.SocialLoginType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTableWithRelationshipAndPictureNmDto {

	private String userId;
	private String name;
	private String email;
	private String nickName;
	private String relationship;
	private Long pictureId;
	private String pictureNm;
	private SocialLoginType loginType;
	private String lastLoginDate;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;

}
