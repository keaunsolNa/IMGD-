package com.nks.imgd.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFriendTableDTO {

    private Long friendId;
    private String userId;
    private String regDtm;
    private String regId;
    private String modDtm;
    private String modId;

}
