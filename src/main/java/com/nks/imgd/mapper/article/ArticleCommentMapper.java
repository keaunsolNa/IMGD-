package com.nks.imgd.mapper.article;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.Schema.ArticleComment;
import com.nks.imgd.dto.dataDTO.ArticleWithTagsAndFiles;

@Mapper
public interface ArticleCommentMapper {

	ArticleComment findArticleCommentByIds(@Param("articleId") Long articleId, @Param("commentId") Long commentId);

	List<ArticleComment> findArticleCommentById(@Param("articleId") Long articleId);

	ArticleWithTagsAndFiles findArticleById(@Param("articleId") Long articleId);

	int insertArticleComment(@Param("article") ArticleComment articleComment);

	int deleteArticleComment(@Param("articleId") Long articleId, @Param("commentId") Long commentId);

}
