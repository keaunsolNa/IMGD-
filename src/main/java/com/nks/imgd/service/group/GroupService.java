package com.nks.imgd.service.group;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.dto.group.GroupTableDTO;
import com.nks.imgd.dto.group.GroupUserDTO;
import com.nks.imgd.mapper.group.GroupTableMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author nks
 * @apiNote Group 관련 작업을 하는 서비스
 * 			해당 서비스는 Group MST_USER 만 접근 가능하다.
 */
@Slf4j
@Service
public class GroupService {

	private final GroupTableMapper groupTableMapper;
	private final CommonMethod commonMethod = new CommonMethod();

	public GroupService(GroupTableMapper groupTableMapper) {
		this.groupTableMapper = groupTableMapper;
	}

	/**
	 * 유저가 가지고 있는 루트 폴더가 생성되지 않은 그룹 목록을 반환한다.
	 *
	 * @param userId 대상 유저 아이디
	 * @return 대상이 가지고 있는 그룹 목록
	 */
	public List<GroupTableDTO> findGroupName(String userId)
	{
		return groupTableMapper.findGroupName(userId);
	}

	/**
	 * 유저가 가지고 있는 그룹 목록을 반환한다.
	 *
	 * @param userId 대상 유저 아이디
	 * @return 대상이 가지고 있는 그룹 목록
	 */
	public List<GroupTableDTO> findGroupWhatInside(String userId)
	{
		List<GroupTableDTO> list = groupTableMapper.findGroupWhatInside(userId);
		for (GroupTableDTO groupTableDTO : list) {
			groupTableDTO.setRegDtm(commonMethod.translateDate(groupTableDTO.getRegDtm()));
		}

		return list;
	}

	/**
	 * 그룹에 속해 있는 유저의 아이디를 조회 한다.
	 *
	 * @param groupId 대상 그룹 아이디
	 * @return 그룹이 가지고 있는 유저 목록
	 */
	public List<GroupUserDTO> findGroupUserWhatInside(String groupId)
	{
		List<GroupUserDTO> list = groupTableMapper.findGroupUserWhatInside(groupId);
		for (GroupUserDTO groupTableDTO : list) {
			groupTableDTO.setRegDtm(commonMethod.translateDate(groupTableDTO.getRegDtm()));
		}

		return list;
	}

	/**
	 * 그룹을 생성 한다.
	 * GroupTable 테이블 생성 후
	 * 생성 유저를 통해 GroupUser Table 도 생성 한다.
	 *
	 * @param dto 그룹 생성 요청 DTO
	 * @return 생성 성공 여부
	 */
	public int makeNewGroup(GroupTableDTO dto) {

		int result = groupTableMapper.makeNewGroup(dto);

		if (result == 1)
		{
			String userId = dto.getGroupMstUserId();
			return groupTableMapper.makeNewGroupUserTable(dto, userId);
		}
		return -1;
	}

	/**
	 * GroupUser 테이블에 Row 추가
	 *
	 * @param dto 그룹 생성 요청 DTO
	 * @param userId 추가할 유저 ID
	 * @return 생성 성공 여부
	 */
	public int makeNewGroupUser(GroupTableDTO dto, String userId) {

		if (groupTableMapper.isUserCheck(dto, userId) > 0) return -1;

		return groupTableMapper.makeNewGroupUserTable(dto, userId);
	}


	/**
	 * GroupUser 테이블에 Row 제거
	 *
	 * @param dto 그룹 제거 요청 DTO
	 * @param userId 삭제할 유저 ID
	 * @return 삭제 성공 여부
	 */
	public int deleteGroupUser(GroupTableDTO dto, String userId) {

		// 해당 그룹에 대상 유저가 존재 하지 않을 경우
		if (groupTableMapper.isUserCheck(dto, userId) <= 0) return -1;

		/*
			삭제 하려는 유저가 MST 유저일 경우,
			그룹의 MST_USER_ID를 변경한 후 삭제 해야 한다.
			단, 그룹의 구성원이 1명 뿐일 경우에는 그룹 자체를 삭제한다.
		 */
		if (dto.getGroupMstUserId().equals(userId)) {

			// 파일 테이블 내 파일 삭제 로직 추가
			if (groupTableMapper.countGroupUser(dto) == 1) return groupTableMapper.deleteGroupTable(dto);
			else return -2;
		}

		// 그 외 일반 유저 삭제는 MST_USER 라면 가능하다.
		return groupTableMapper.deleteGroupUser(dto, userId);
	}


	/**
	 * GroupUser 테이블 MST_USER_ID 변경
	 *
	 * @param dto 그룹 MST_USER 변경 요청 DTO
	 * @param userId MST_USER 될 ID
	 * @return 삭제 성공 여부
	 */
	public int changeMstUserGroup(GroupTableDTO dto, String userId) {

		// 해당 그룹에 대상 유저가 존재하지 않을 경우
		if (groupTableMapper.isUserCheck(dto, userId) > 0) return -1;

		// 그 외 일반 유저 삭제는 MST_USER 라면 가능하다.
		return groupTableMapper.changeMstUserGroup(dto, userId);
	}



}
