package com.nks.imgd.mapper.file;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nks.imgd.dto.data.GroupTableWithMstUserNameDto;
import com.nks.imgd.dto.data.MakeDirDto;
import com.nks.imgd.dto.data.MakeFileDto;
import com.nks.imgd.dto.schema.FileTable;

@Mapper
public interface FileTableMapper {

	FileTable findRootPath(@Param("fileId") Long fileId);

	FileTable findFileIdByFileOrgNmInDirCase(@Param("group") GroupTableWithMstUserNameDto groupTableWithMstUserNameDto);

	FileTable findFileNmByDirId(@Param("dirId") Long dirId);

	FileTable findFileById(@Param("fileId") Long fileId);

	FileTable findUserProfileFileId(@Param("userId") String userId);

	List<FileTable> findFileAndDirectory(@Param("parentId") Long parentId, @Param("groupId") Long groupId);

	List<FileTable> findFileByGroupId(@Param("groupId") Long groupId);

	List<FileTable> findFileByParentId(@Param("fileId") Long fileId);

	int makeGroupDir(@Param("group") GroupTableWithMstUserNameDto groupTableWithMstUserNameDto);

	int makeDir(@Param("dto") MakeDirDto dto);

	int makeFile(@Param("dto") MakeFileDto dto);

	int makeUserProfileImg(@Param("file") FileTable file, @Param("userId") String userId);

	int deleteById(@Param("fileId") Long fileId);

	int deleteFilesByGroupId(@Param("groupId") Long groupId);
}
