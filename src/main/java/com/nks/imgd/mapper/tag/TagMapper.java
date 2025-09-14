package com.nks.imgd.mapper.tag;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.Schema.Tag;

@Mapper
public interface TagMapper {

	List<Tag> findAllTag();

	Tag selectTagById(@Param("tagId") Long tagId);

	List<Tag> selectTagByLikeName(@Param("name") String name);

	List<Tag> selectTagWhatInTarget(@Param("tagId") String tagId);

	int makeNewTag(@Param("userId") String userId, @Param("tag") Tag tag);
}
