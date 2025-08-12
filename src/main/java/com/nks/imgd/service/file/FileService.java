package com.nks.imgd.service.file;

import java.io.File;

import com.nks.imgd.dto.file.FileTableDTO;
import com.nks.imgd.dto.group.GroupTableDTO;
import com.nks.imgd.mapper.file.FileTableMapper;

import org.springframework.stereotype.Service;

/**
 * @author nks
 * @apiNote File 관련 작업을 하는 서비스
 * 			그룹 권한에 연계 되어 접근 가능 범위를 지정 한다.
 * 	        파일 압축 알고리즘 고려
 * <p>
 * 환경 변수에 지정된 RootPath 를 시작점으로,
 * directory 루트 구조는 다음과 같다
 * RootPath
 *      └──────Group
 *          └────── 각 그룹별 directory (groupId_groupNm, 1_테스트 그룹)
 *              └────── 그룹 내 개별 directory (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                  └────── 그룹 내 개별 directory (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                  └────── 개별 IMG 파일
 *                  └────── 개별 IMG 파일
 *                  └────── 개별 IMG 파일
 *              └────── 그룹 내 개별 directory (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                  └────── 그룹 내 개별 directory (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                  └────── 개별 IMG 파일
 *                  └────── 개별 IMG 파일
 *                      └────── 그룹 내 개별 directory (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                      └────── 개별 IMG 파일
 *                      └────── 개별 IMG 파일
 *          └────── 각 그룹별 directory (groupId_groupNm, 2_테스트그룹2)
 *      └──────Personal
 *          └────── 각 계정별 directory (user_id)
 *              └────── 계정별 프로필 사진, 필요시 HISTORY 기능 추가.
 * </p>
 */
@Service
public class FileService {

    private final FileTableMapper fileTableMapper;
    public FileService(FileTableMapper fileTableMapper) {
        this.fileTableMapper = fileTableMapper;
    }

    public String selectRootPath(Long fileId) {

        StringBuilder sb = new StringBuilder();

        Long cur = fileId;

        while (cur != null) {
            FileTableDTO r = fileTableMapper.selectRootPath(cur);
            if (r == null) break;
            if (r.getFilePath() != null) {
                sb.insert(0, "/" + r.getFilePath());
            }
            cur = r.getParentId();
        }
        return sb.toString();

    }

    public String selectFileNmById(Long fileId) {

        return fileTableMapper.selectFileNmById(fileId).getFileNm();
    }

    /**
     * @param dto directory 생성할 그룹
     */
    public int makeGroupDir(GroupTableDTO dto)
    {

        int result = fileTableMapper.makeGroupDir(dto);

        if (result == 1)
        {

            FileTableDTO fileDTO = fileTableMapper.selectFileIdByFileOrgNmInDirCase(dto);
            String path = selectRootPath(fileDTO.getFileId());

            File file = new File(path + "/" + dto.getGroupId() + "_" + dto.getGroupNm());

            file.mkdirs();

            return 1;
        }

        return -1;
    }

    public int makeDir(String userId, Long parentId, String dirNm)
    {

        String path = selectRootPath(parentId);

        int result = fileTableMapper.makeDir(userId, dirNm, selectFileNmById(parentId), parentId);

        if (result == 1)
        {

            File file = new File(path + "/" + selectFileNmById(parentId) + "/" + dirNm);
            file.mkdirs();

            return 1;
        }

        return -1;
    }

}
