package com.nks.imgd.mapper.file;

import com.nks.imgd.dto.file.FileTableDTO;
import com.nks.imgd.dto.group.GroupTableDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileTableMapper {

    FileTableDTO selectRootPath(@Param("fileId") Long fileId);

    FileTableDTO selectFileIdByFileOrgNmInDirCase(@Param("group") GroupTableDTO groupTableDTO);

    FileTableDTO selectFileNmByDirId(@Param("dirId") Long dirId);

    int makeGroupDir(@Param("group")GroupTableDTO groupTableDTO);

    int makeDir(@Param("userId") String userId
              , @Param("dirNm") String dirNm
              , @Param("path") String path
              , @Param("parentId") Long parentId
			  , @Param("groupId") Long groupId);

	int makeFile(@Param("fileNm") String fileNm
			   , @Param("fileOrgNm") String fileOrgNm
		       , @Param("path") String path
	  		   , @Param("parentId") Long parentId
			   , @Param("groupId") Long groupId
	  		   , @Param("userId") String userId);
}
