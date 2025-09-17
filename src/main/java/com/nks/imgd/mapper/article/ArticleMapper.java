package com.nks.imgd.mapper.article;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.dataDTO.ArticleWithTags;
import com.nks.imgd.dto.searchDTO.ArticleSearch;

@Mapper
public interface ArticleMapper {

	List<ArticleWithTags> findAllArticle(ArticleSearch articleSearch);

	ArticleWithTags findArticleById(@Param("articleId") Long articleId);

	int makeNewArticle(@Param("article") ArticleWithTags article);

	int increaseArticleWatchCnt(@Param("articleId") Long articleId);

	int deleteArticle(@Param("articleId") Long articleId);
}
