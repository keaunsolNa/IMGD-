package com.nks.imgd.dto.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileTableDTO {

	private Long fileId;
	private String fileNm;
	private String fileOrgNm;
	private String filePath;
	private String type;
	private Long parentId;
	private Long groupId;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;
}
