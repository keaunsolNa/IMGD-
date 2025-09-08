package com.nks.imgd.dto.dataDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeDirDTO {
	private Long fileId;
	private String userId;
	private Long parentId;
	private String dirNm;
	private Long groupId;
	private String path;
}
