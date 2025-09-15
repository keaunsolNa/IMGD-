package com.nks.imgd.dto.dataDTO;

import java.util.List;

import com.nks.imgd.dto.Enum.ArticleType;
import com.nks.imgd.dto.Schema.Tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleWithTags {

	private Long articleId;
	private String postPwd;
	private ArticleType type;
	private String tagIds;
	private List<Tag> tagList;
	private String userId;
	private String userNm;
	private String title;
	private String article;
	private Long like;
	private Long watch;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;
}
