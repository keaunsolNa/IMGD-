package com.nks.imgd.dto.schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

	private Long tagId;
	private String name;
	private String description;
	private String color;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;
}
