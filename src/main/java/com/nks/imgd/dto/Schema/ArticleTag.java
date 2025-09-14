package com.nks.imgd.dto.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleTag {

	private long articleId;
	private long tagId;
}
