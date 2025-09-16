package com.nks.imgd.mapper.article;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.Schema.ArticleTag;

@Mapper
public interface ArticleTagMapper {

	ArticleTag findArticleTagById(@Param("articleId") Long articleId, @Param("tagId") Long tagId);

	int makeNewArticleTag(@Param("articleTag") ArticleTag articleTag);

}
