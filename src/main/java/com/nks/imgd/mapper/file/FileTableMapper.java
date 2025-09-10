package com.nks.imgd.mapper.file;

import java.util.List;

import com.nks.imgd.dto.dataDTO.MakeDirDTO;
import com.nks.imgd.dto.dataDTO.MakeFileDTO;
import com.nks.imgd.dto.Schema.FileTableDTO;
import com.nks.imgd.dto.dataDTO.GroupTableWithMstUserNameDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileTableMapper {

    FileTableDTO findRootPath(@Param("fileId") Long fileId);

    FileTableDTO findFileIdByFileOrgNmInDirCase(@Param("group") GroupTableWithMstUserNameDTO groupTableWithMstUserNameDTO);

    FileTableDTO findFileNmByDirId(@Param("dirId") Long dirId);

	FileTableDTO findFileById(@Param("fileId") Long fileId);

	List<FileTableDTO> findFileAndDirectory(@Param("parentId") Long parentId
										  , @Param("groupId") Long groupId);

    List<FileTableDTO> findFileByGroupId(@Param("groupId") Long groupId);

    List<FileTableDTO> findFileByParentId(@Param("fileId") Long fileId);

    int makeGroupDir(@Param("group") GroupTableWithMstUserNameDTO groupTableWithMstUserNameDTO);

    int makeDir(@Param("dto") MakeDirDTO dto);

	int makeFile(@Param("dto") MakeFileDTO dto);

	int makeUserProfileImg(@Param("file") FileTableDTO file
						 , @Param("userId") String userId);

	int deleteById(@Param("fileId") Long fileId);

    int deleteFilesByGroupId(@Param("groupId") Long groupId);
}
