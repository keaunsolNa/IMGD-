package com.nks.imgd.mapper.file;

import java.util.List;

import com.nks.imgd.dto.data.MakeDirDTO;
import com.nks.imgd.dto.data.MakeFileDTO;
import com.nks.imgd.dto.file.FileTableDTO;
import com.nks.imgd.dto.group.GroupTableDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileTableMapper {

    FileTableDTO findRootPath(@Param("fileId") Long fileId);

    FileTableDTO findFileIdByFileOrgNmInDirCase(@Param("group") GroupTableDTO groupTableDTO);

    FileTableDTO findFileNmByDirId(@Param("dirId") Long dirId);

	FileTableDTO findFileById(@Param("fileId") Long fileId);

	List<FileTableDTO> findFileAndDirectory(@Param("parentId") Long parentId
										  , @Param("groupId") Long groupId);

    int makeGroupDir(@Param("group")GroupTableDTO groupTableDTO);

    int makeDir(@Param("dto") MakeDirDTO dto);

	int makeFile(@Param("dto") MakeFileDTO dto);

	int makeUserProfileImg(@Param("file") FileTableDTO file
						 , @Param("userId") String userId);

	int deleteById(@Param("fileId") Long fileId);
}
