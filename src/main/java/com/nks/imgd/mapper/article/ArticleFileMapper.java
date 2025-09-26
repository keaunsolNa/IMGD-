package com.nks.imgd.mapper.article;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleFileMapper {

    int makeArticleFile(@Param("articleId") Long articleId, @Param("fileId") Long fileId);
}
