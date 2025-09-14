package com.nks.imgd.service.group;

import java.util.List;
import java.util.Map;

import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.Enum.ResponseMsg;
import com.nks.imgd.dto.Enum.Role;
import com.nks.imgd.service.file.FileService;
import com.nks.imgd.service.user.UserProfilePort;
import org.springframework.stereotype.Service;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.dto.dataDTO.GroupTableWithMstUserNameDTO;
import com.nks.imgd.dto.dataDTO.GroupUserWithNameDTO;
import com.nks.imgd.mapper.group.GroupTableMapper;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author nks
 * @apiNote Group 관련 작업을 하는 서비스
 * 			접근 제한 :  Group MST_USER
 */
@Service
public class GroupService {

	private final GroupTableMapper groupTableMapper;
    private final UserProfilePort userProfilePort;
	private final FileService fileService;
	private static final CommonMethod commonMethod = new CommonMethod();

    public GroupService(GroupTableMapper groupTableMapper, UserProfilePort userProfilePort, FileService fileService) {
		this.groupTableMapper = groupTableMapper;
        this.userProfilePort = userProfilePort;
		this.fileService = fileService;
	}

	/**
	 * 유저가 가지고 있는 루트 폴더가 생성 되지 않은 그룹 목록을 반환 한다.
	 *
	 * @param userId 대상 유저 아이디
	 * @return 대상이 가지고 있는 그룹 목록
	 */
	public List<GroupTableWithMstUserNameDTO> findGroupName(String userId)
	{
		return groupTableMapper.findGroupName(userId);
	}

	/**
	 * 유저가 가지고 있는 그룹 목록을 반환 한다.
	 *
	 * @param userId 대상 유저 아이디
	 * @return 대상이 가지고 있는 그룹 목록
	 */
	public List<GroupTableWithMstUserNameDTO> findGroupWhatInside(String userId)
	{
		return postProcessingGroupTables(groupTableMapper.findGroupWhatInside(userId));
	}

	/**
	 * 그룹에 속해 있는 유저의 아이디 반환 한다.
	 *
	 * @param groupId 대상 그룹 아이디
	 * @return 그룹이 가지고 있는 유저 목록
	 */
	public List<GroupUserWithNameDTO> findGroupUserWhatInside(String userId, Long groupId)
	{
		return postProcessingGroupUserTables(groupTableMapper.findGroupUserWhatInside(userId, groupId));
	}

    /**
     * 그룹 아이디를 통해 그룹 정보를 반환 한다.
     * 
     * @param groupId 대상 그룹 아이디
     * @return 그룹 정보
     */
    public GroupTableWithMstUserNameDTO findGroupByGroupId(Long groupId) {
        return groupTableMapper.findGroupByGroupId(groupId);
    }

	/**
	 * 그룹을 생성 한다.
	 * GroupTable 테이블 생성 후
	 * 생성 유저를 통해 GroupUser Table 도 생성 한다.
	 *
	 * @param dto 그룹 생성 요청 DTO
	 * @return 생성 성공 여부
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<GroupTableWithMstUserNameDTO> createGroup(GroupTableWithMstUserNameDTO dto) {

        Role role = Role.valueOf(userProfilePort.findHighestUserRole(dto.getGroupMstUserId()).getRoleNm());
        int canMakeGroupValue = role.getPermissionOfMakeGroup();
        int totalCountOfMakeGroup = groupTableMapper.findAllGroupWhatUserMake(dto.getGroupMstUserId());

        if (canMakeGroupValue <= totalCountOfMakeGroup) return ServiceResult.failure(ResponseMsg.GROUP_LIMIT_EXCEEDED);

		if (groupTableMapper.makeNewGroup(dto) != 1) return ServiceResult.failure(ResponseMsg.BAD_REQUEST);

        ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
                groupTableMapper.makeNewGroupUserTable(dto, dto.getGroupMstUserId())
        );

        if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
            return ServiceResult.failure(fsMsg);
        }

        return ServiceResult.success(() -> findGroupByGroupId(dto.getGroupId()));
	}

	/**
	 * GroupUser 그룹 구성원 추가
	 *
	 * @param dto 그룹 생성 요청 DTO
	 * @param userId 추가할 유저 ID
	 * @return 생성 성공 여부
	 */
    @Transactional(rollbackFor = Exception.class)
	public ServiceResult<List<GroupUserWithNameDTO>> makeNewGroupUser(GroupTableWithMstUserNameDTO dto, String userId) {

        // 이미 가입 된 인원 추가 요청 시
		if (groupTableMapper.isUserCheck(dto, userId) > 0) return ServiceResult.failure(ResponseMsg.ALREADY_JOIN_USER);

        ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
                groupTableMapper.makeNewGroupUserTable(dto, userId)
        );

        if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
            return ServiceResult.failure(fsMsg);
        }

        return ServiceResult.success(() -> findGroupUserWhatInside(userId, dto.getGroupId()));
	}


	/**
	 * GroupUser 그룹 구성원 제거.
	 *
	 * @param dto 그룹 제거 요청 DTO
	 * @param userId 삭제할 유저 ID
	 * @return 삭제 성공 여부
	 */
    @Transactional(rollbackFor = Exception.class)
	public ServiceResult<List<GroupUserWithNameDTO>> deleteGroupUser(GroupTableWithMstUserNameDTO dto, String userId) {

		// 해당 그룹에 대상 유저가 존재 하지 않을 경우
		if (groupTableMapper.isUserCheck(dto, userId) <= 0) return ServiceResult.failure(ResponseMsg.CAN_NOT_FIND_USER,
			Map.of("userId", userId));

		/*
			삭제 하려는 유저가 MST 유저일 경우,
			그룹의 MST_USER_ID를 변경한 후 삭제 해야 한다.
			단, 그룹의 구성원이 1명 뿐일 경우, 그룹 자체를 삭제 한다.
		 */
		if (dto.getGroupMstUserId().equals(userId)) {

            // 그룹 구성원이 한 명이고, 시행 유저가 그룹의 MST_USER 라면
			if (groupTableMapper.countGroupUser(dto) == 1 && groupTableMapper.deleteGroupTable(dto.getGroupId()) == 1) {

                // 그룹 유저를 삭제한 뒤
                ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
                        groupTableMapper.deleteGroupUser(dto, userId)
                );

                if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
                    return ServiceResult.failure(fsMsg);
                }

                // 그룹에 속한 모든 폴더 / 파일을 삭제한다.
                return deleteGroup(userId, dto.getGroupId());

			}
			else return ServiceResult.failure(ResponseMsg.GROUP_MST_USER_CANT_DELETE);
		}

        ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
                groupTableMapper.deleteGroupUser(dto, userId)
        );

        if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
            return ServiceResult.failure(fsMsg);
        }

        return ServiceResult.success(() -> findGroupUserWhatInside(userId, dto.getGroupId()));

	}


	/**
	 * GroupUser 테이블 MST_USER_ID 변경
	 *
	 * @param dto 그룹 MST_USER 변경 요청 DTO
	 * @param userId MST_USER 될 ID
	 * @return 삭제 성공 여부
	 */
    @Transactional(rollbackFor = Exception.class)
	public ServiceResult<List<GroupUserWithNameDTO>>  changeMstUserGroup(GroupTableWithMstUserNameDTO dto, String userId) {

		// 해당 그룹에 대상 유저가 없을 경우
		if (groupTableMapper.isUserCheck(dto, userId) > 0) return ServiceResult.failure(ResponseMsg.CAN_NOT_FIND_USER, Map.of("userId", userId));

        ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
                groupTableMapper.changeMstUserGroup(dto, userId)
        );

        if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
            return ServiceResult.failure(fsMsg);
        }

        return ServiceResult.success(() -> findGroupUserWhatInside(userId, dto.getGroupId()));
	}

    /**
     * 그룹을 삭제한다.
     * @param userId 삭제하려는 유저의 아이디
     * @param groupId 삭제하려는 그룹의 아이디
     * @return 그룹과 그 안의 폴더 / 파일을 모두 삭제한 후 대상 유저가 가진 그룹 목록을 반환
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResult<List<GroupUserWithNameDTO>> deleteGroup(String userId, Long groupId) {

        // 해당 그룹 아이디를 가진 모든 파일 / 폴더의 물리 / DB 내용 삭제
        ServiceResult<List<GroupTableWithMstUserNameDTO>> result = fileService.deleteFilesByGroupId(userId, groupId);

       if (!result.status().equals(ResponseMsg.ON_SUCCESS)) {
           return ServiceResult.failure(ResponseMsg.FILE_DELETE_FAILED);
       }

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			groupTableMapper.deleteGroupTable(groupId)
		);

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

        return ServiceResult.success(() -> findGroupUserWhatInside(userId, groupId));
    }

	// ───────────────────────────────── helper methods ───────────────────────────────

	/**
	 * 그릅 목록 반환 시 후처리 진행 한다.
	 * DTM(YYYYmmDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 *
	 * @param groups 대상 그룹 리스트
	 * @return 후처리 후 대상 그룹 리스트
	 */
	public List<GroupTableWithMstUserNameDTO> postProcessingGroupTables(List<GroupTableWithMstUserNameDTO> groups) {

		for (GroupTableWithMstUserNameDTO group : groups) {
			postProcessingGroupTable(group);
		}

		return groups;
	}

	/**
	 * 그룹 반환 시 후처리 진행 한다.
	 * DTM(YYYYmmDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 *
	 * @param group 대상 그룹
	 */
	public void postProcessingGroupTable(GroupTableWithMstUserNameDTO group) {

		group.setRegDtm(null != group.getRegDtm() ? commonMethod.translateDate(group.getRegDtm()) : null);
		group.setModDtm(null != group.getModDtm() ? commonMethod.translateDate(group.getModDtm()) : null);
	}

	/**
	 * 그릅 유저 목록 반환 시 후처리 진행 한다.
	 * DTM(YYYYmmDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 *
	 * @param groups 대상 그룹 유저 리스트
	 * @return 후처리 후 대상 그룹 유저 리스트
	 */
	public List<GroupUserWithNameDTO> postProcessingGroupUserTables(List<GroupUserWithNameDTO> groups) {

		for (GroupUserWithNameDTO group : groups) {
			postProcessingGroupUserTable(group);
		}

		return groups;
	}

	/**
	 * 그룹 반환 시 후처리 진행 한다.
	 * DTM(YYYYmmDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 *
	 * @param group 대상 그룹
	 */
	public void postProcessingGroupUserTable(GroupUserWithNameDTO group) {

		group.setRegDtm(null != group.getRegDtm() ? commonMethod.translateDate(group.getRegDtm()) : null);
		group.setModDtm(null != group.getModDtm() ? commonMethod.translateDate(group.getModDtm()) : null);
	}

}
