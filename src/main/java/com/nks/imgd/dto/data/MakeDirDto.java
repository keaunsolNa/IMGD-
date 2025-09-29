package com.nks.imgd.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeDirDto {
	private Long fileId;
	private String userId;
	private Long parentId;
	private String dirNm;
	private Long groupId;
	private String path;
}
