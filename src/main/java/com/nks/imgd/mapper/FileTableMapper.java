package com.nks.imgd.mapper;

import com.nks.imgd.dto.FileTableDTO;
import com.nks.imgd.dto.GroupTableDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileTableMapper {

    FileTableDTO selectRootPath(@Param("fileId") Long fileId);

    FileTableDTO selectFileIdByFileOrgNmInDirCase(@Param("group") GroupTableDTO groupTableDTO);

    FileTableDTO selectFileNmById(@Param("fileId") Long fileId);


    int makeGroupDir(@Param("group")GroupTableDTO groupTableDTO);

    int makeDir(@Param("userId") String userId
              , @Param("dirNm") String dirNm
              , @Param("path") String path
              , @Param("parentId") Long parentId);
}
