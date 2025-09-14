package com.nks.imgd.mapper.file;

import java.util.List;

import com.nks.imgd.dto.dataDTO.MakeDirDTO;
import com.nks.imgd.dto.dataDTO.MakeFileDTO;
import com.nks.imgd.dto.Schema.FileTable;
import com.nks.imgd.dto.dataDTO.GroupTableWithMstUserNameDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileTableMapper {

    FileTable findRootPath(@Param("fileId") Long fileId);

    FileTable findFileIdByFileOrgNmInDirCase(@Param("group") GroupTableWithMstUserNameDTO groupTableWithMstUserNameDTO);

    FileTable findFileNmByDirId(@Param("dirId") Long dirId);

	FileTable findFileById(@Param("fileId") Long fileId);

	List<FileTable> findFileAndDirectory(@Param("parentId") Long parentId
										  , @Param("groupId") Long groupId);

    List<FileTable> findFileByGroupId(@Param("groupId") Long groupId);

    List<FileTable> findFileByParentId(@Param("fileId") Long fileId);

    int makeGroupDir(@Param("group") GroupTableWithMstUserNameDTO groupTableWithMstUserNameDTO);

    int makeDir(@Param("dto") MakeDirDTO dto);

	int makeFile(@Param("dto") MakeFileDTO dto);

	int makeUserProfileImg(@Param("file") FileTable file
						 , @Param("userId") String userId);

	int deleteById(@Param("fileId") Long fileId);

    int deleteFilesByGroupId(@Param("groupId") Long groupId);
}
