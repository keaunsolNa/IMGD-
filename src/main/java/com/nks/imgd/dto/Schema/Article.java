package com.nks.imgd.dto.Schema;

import com.nks.imgd.dto.Enum.ArticleType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {

	private Long articleId;
	private String postPwd;
	private ArticleType type;
	private String userId;
	private String title;
	private String article;
	private Long like;
	private Long watch;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;

}
