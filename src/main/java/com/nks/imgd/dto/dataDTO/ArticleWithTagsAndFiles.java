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
public class ArticleWithTagsAndFiles {

	private Long articleId;
	private ArticleType type;
	private String tagIds;
	private List<Tag> tagList;
	private List<ArticleWithTagsAndFiles> comments;
    private List<MakeFileDTO> files;
	private String userId;
	private String pictureNm;
	private String userNm;
	private String title;
	private String article;
	private Long likeCnt;
	private Long watchCnt;
	private Long commentCnt;
	private String regDtm;
	private String regId;
	private String modDtm;
	private String modId;
}
