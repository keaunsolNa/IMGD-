package com.nks.imgd.service.file;

import com.nks.imgd.dto.GroupTableDTO;
import com.nks.imgd.mapper.FileTableMapper;
import org.springframework.stereotype.Service;

/**
 * @author nks
 * @apiNote File 관련 작업을 하는 서비스
 * 			그룹 권한에 연계되어 접근 가능 범위를 지정한다.
 * 	        파일 압축 알고리즘 고려
 * <p>
 * 환경 변수에 지정된 RootPath 를 시작점으로,
 * 디렉토리의 루트 구조는 다음과 같다
 * RootPath
 *      └──────Group
 *          └────── 각 그룹별 디렉터리 (groupId_groupNm, 1_테스트그룹)
 *              └────── 그룹 내 개별 디렉터리 (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                  └────── 그룹 내 개별 디렉터리 (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                  └────── 개별 IMG 파일
 *                  └────── 개별 IMG 파일
 *                  └────── 개별 IMG 파일
 *              └────── 그룹 내 개별 디렉터리 (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                  └────── 그룹 내 개별 디렉터리 (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                  └────── 개별 IMG 파일
 *                  └────── 개별 IMG 파일
 *                      └────── 그룹 내 개별 디렉터리 (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                      └────── 개별 IMG 파일
 *                      └────── 개별 IMG 파일
 *          └────── 각 그룹별 디렉터리 (groupId_groupNm, 2_테스트그룹2)
 *      └──────Personal
 *          └────── 각 계정별 디렉터리 (user_id)
 *              └────── 계정별 프로필 사진, 필요시 HISTORY 기능 추가.
 * </p>
 */
@Service
public class FileService {

    private final FileTableMapper fileTableMapper;

    public FileService(FileTableMapper fileTableMapper) {
        this.fileTableMapper = fileTableMapper;
    }

    /**

     *
     *
     * @param dto
     */
    public int makeGroupDir(GroupTableDTO dto)
    {
        int result = fileTableMapper.makeGroupDir(dto);

        if (result == 1)
        {
            return 0;
        }
        return -1;
    }

}
