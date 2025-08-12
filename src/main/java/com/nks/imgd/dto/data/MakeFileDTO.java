package com.nks.imgd.dto.data;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeFileDTO {

	private Long folderId;
	private String userId;
	private Long groupId;
	private String fileName;
	private MultipartFile originalFile;
}
