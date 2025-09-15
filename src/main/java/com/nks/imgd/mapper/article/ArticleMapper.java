package com.nks.imgd.mapper.article;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.dataDTO.ArticleWithTags;

@Mapper
public interface ArticleMapper {

	List<ArticleWithTags> findAllArticle();

	int makeNewArticle(@Param("article") ArticleWithTags article);

	int likeArticle(@Param("articleId") Long articleId);
}
