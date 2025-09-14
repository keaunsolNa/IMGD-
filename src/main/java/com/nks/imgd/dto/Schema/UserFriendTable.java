package com.nks.imgd.dto.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFriendTable {

    private Long friendId;
    private String userId;
	private String relationship;
    private String regDtm;
    private String regId;
    private String modDtm;
    private String modId;

}
