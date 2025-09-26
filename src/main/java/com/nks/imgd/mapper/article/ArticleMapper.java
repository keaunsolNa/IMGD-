package com.nks.imgd.mapper.article;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.dataDTO.ArticleWithTagsAndFiles;
import com.nks.imgd.dto.searchDTO.ArticleSearch;

@Mapper
public interface ArticleMapper {

	List<ArticleWithTagsAndFiles> findAllArticle(ArticleSearch articleSearch);

	ArticleWithTagsAndFiles findArticleById(@Param("articleId") Long articleId);

	int makeNewArticle(@Param("article") ArticleWithTagsAndFiles article);

	int increaseArticleWatchCnt(@Param("articleId") Long articleId);

	int deleteArticle(@Param("articleId") Long articleId);
}
