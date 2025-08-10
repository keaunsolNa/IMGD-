package com.nks.imgd.mapper;

import com.nks.imgd.dto.GroupTableDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileTableMapper {

    int makeGroupDir(@Param("group")GroupTableDTO groupTableDTO);

    int makeDir(@Param("group")GroupTableDTO groupTableDTO
              , @Param("dirNm") String dirNm
              , @Param("parentId") String parentId);
}
