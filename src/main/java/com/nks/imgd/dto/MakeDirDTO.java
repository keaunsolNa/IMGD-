package com.nks.imgd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeDirDTO {
	private String userId;
	private Long parentId;
	private String dirNm;
}
