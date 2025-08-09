package com.nks.imgd.service.group;

import org.springframework.stereotype.Service;

import com.nks.imgd.dto.GroupTableDTO;
import com.nks.imgd.mapper.GroupTableMapper;

/**
 * @author nks
 * @apiNote Group 관련 작업을 하는 서비스
 * 			해당 서비스는 Group MST_USER 만 접근 가능하다.
 */
@Service
public class GroupService {

	private final GroupTableMapper groupTableMapper;

	public GroupService(GroupTableMapper groupTableMapper) {
		this.groupTableMapper = groupTableMapper;
	}

	/**
	 * 그룹을 생성한다.
	 * GroupTable 테이블 생성 후
	 * 생성 유저를 통해 GroupUser Table 도 생성한다.
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

		// 해당 그룹에 대상 유저가 존재하지 않을 경우
		if (groupTableMapper.isUserCheck(dto, userId) <= 0) return -1;

		/*
			삭제하려는 유저가 MST 유저일 경우,
			그룹의 MST_USER_ID를 변경한 후 삭제해야 한다.
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
