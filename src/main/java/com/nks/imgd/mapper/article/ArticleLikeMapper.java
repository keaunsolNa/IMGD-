package com.nks.imgd.mapper.article;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.Schema.ArticleLike;

@Mapper
public interface ArticleLikeMapper {

	ArticleLike findArticleLikeWhatIds (@Param("articleId") Long articleId, @Param("userId") String userId);

	int likeArticle (@Param("articleId") Long articleId, @Param("userId") String userId );

	int unLikeArticle (@Param("articleId") Long articleId, @Param("userId") String userId );

}
