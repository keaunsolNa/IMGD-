package com.nks.imgd.service.group;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.dto.group.GroupTableDTO;
import com.nks.imgd.dto.group.GroupUserDTO;
import com.nks.imgd.mapper.group.GroupTableMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author nks
 * @apiNote Group 관련 작업을 하는 서비스
 * 			접근 제한 :  Group MST_USER
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
	 * 유저가 가지고 있는 루트 폴더가 생성 되지 않은 그룹 목록을 반환 한다.
	 *
	 * @param userId 대상 유저 아이디
	 * @return 대상이 가지고 있는 그룹 목록
	 */
	public List<GroupTableDTO> findGroupName(String userId)
	{
		return groupTableMapper.findGroupName(userId);
	}

	/**
	 * 유저가 가지고 있는 그룹 목록을 반환 한다.
	 *
	 * @param userId 대상 유저 아이디
	 * @return 대상이 가지고 있는 그룹 목록
	 */
	public List<GroupTableDTO> findGroupWhatInside(String userId)
	{
		return postProcessingGroupTables(groupTableMapper.findGroupWhatInside(userId));
	}

	/**
	 * 그룹에 속해 있는 유저의 아이디 반환 한다.
	 *
	 * @param groupId 대상 그룹 아이디
	 * @return 그룹이 가지고 있는 유저 목록
	 */
	public List<GroupUserDTO> findGroupUserWhatInside(Long groupId)
	{
		return postProcessingGroupUserTables(groupTableMapper.findGroupUserWhatInside(groupId));
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
	public ResponseEntity<List<GroupUserDTO>> makeNewGroup(GroupTableDTO dto) {

		if (groupTableMapper.makeNewGroup(dto) != 1) return ResponseEntity.badRequest().build();
		return returnResultWhenTransaction(groupTableMapper.makeNewGroupUserTable(dto, dto.getGroupMstUserId()), () -> findGroupUserWhatInside(dto.getGroupId()));
	}

	/**
	 * GroupUser 그룹 구성원 추가
	 *
	 * @param dto 그룹 생성 요청 DTO
	 * @param userId 추가할 유저 ID
	 * @return 생성 성공 여부
	 */
	public ResponseEntity<List<GroupUserDTO>> makeNewGroupUser(GroupTableDTO dto, String userId) {

		if (groupTableMapper.isUserCheck(dto, userId) > 0) return ResponseEntity.badRequest().build();
		return returnResultWhenTransaction(groupTableMapper.makeNewGroupUserTable(dto, userId), () -> findGroupUserWhatInside(dto.getGroupId()));

	}


	/**
	 * GroupUser 그룹 구성원 제거.
	 *
	 * @param dto 그룹 제거 요청 DTO
	 * @param userId 삭제할 유저 ID
	 * @return 삭제 성공 여부
	 */
	public ResponseEntity<List<GroupUserDTO>> deleteGroupUser(GroupTableDTO dto, String userId) {

		// 해당 그룹에 대상 유저가 존재 하지 않을 경우
		if (groupTableMapper.isUserCheck(dto, userId) <= 0) return ResponseEntity.badRequest().build();

		/*
			삭제 하려는 유저가 MST 유저일 경우,
			그룹의 MST_USER_ID를 변경한 후 삭제 해야 한다.
			단, 그룹의 구성원이 1명 뿐일 경우, 그룹 자체를 삭제 한다.
		 */
		if (dto.getGroupMstUserId().equals(userId)) {

			// TODO 파일 테이블 내 파일 삭제 로직 추가
			if (groupTableMapper.countGroupUser(dto) == 1 && groupTableMapper.deleteGroupTable(dto) == 1) {
				return ResponseEntity.ok().body(null);
			}
			else ResponseEntity.badRequest().build();
		}

		// 그 외 일반 유저 삭제는 MST_USER 라면 가능.
		return returnResultWhenTransaction(groupTableMapper.deleteGroupUser(dto, userId), () -> findGroupUserWhatInside(dto.getGroupId()));
	}


	/**
	 * GroupUser 테이블 MST_USER_ID 변경
	 *
	 * @param dto 그룹 MST_USER 변경 요청 DTO
	 * @param userId MST_USER 될 ID
	 * @return 삭제 성공 여부
	 */
	public ResponseEntity<List<GroupUserDTO>> changeMstUserGroup(GroupTableDTO dto, String userId) {

		// 해당 그룹에 대상 유저가 없을 경우
		if (groupTableMapper.isUserCheck(dto, userId) > 0) return ResponseEntity.badRequest().build();

		// 마스터 유저 변경
		return returnResultWhenTransaction(groupTableMapper.changeMstUserGroup(dto, userId), () -> findGroupUserWhatInside(dto.getGroupId()));
	}

	// ───────────────────────────────── helper methods ───────────────────────────────

	/**
	 * 그릅 목록 반환 시 후처리 진행 한다.
	 * DTM(YYYYMMDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 *
	 * @param groups 대상 그룹 리스트
	 * @return 후처리 후 대상 그룹 리스트
	 */
	public List<GroupTableDTO> postProcessingGroupTables(List<GroupTableDTO> groups) {

		for (GroupTableDTO group : groups) {
			postProcessingGroupTable(group);
		}

		return groups;
	}

	/**
	 * 그룹 반환 시 후처리 진행 한다.
	 * DTM(YYYYMMDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 *
	 * @param group 대상 그룹
	 */
	public void postProcessingGroupTable(GroupTableDTO group) {

		group.setRegDtm(group.getRegDtm() != null ? commonMethod.translateDate(group.getRegDtm()) : null);
		group.setModDtm(group.getModDtm() != null ? commonMethod.translateDate(group.getModDtm()) : null);
	}

	/**
	 * 그릅 유저 목록 반환 시 후처리 진행 한다.
	 * DTM(YYYYMMDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 *
	 * @param groups 대상 그룹 유저 리스트
	 * @return 후처리 후 대상 그룹 유저 리스트
	 */
	public List<GroupUserDTO> postProcessingGroupUserTables(List<GroupUserDTO> groups) {

		for (GroupUserDTO group : groups) {
			postProcessingGroupUserTable(group);
		}

		return groups;
	}

	/**
	 * 그룹 반환 시 후처리 진행 한다.
	 * DTM(YYYYMMDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 *
	 * @param group 대상 그룹
	 */
	public void postProcessingGroupUserTable(GroupUserDTO group) {

		group.setRegDtm(group.getRegDtm() != null ? commonMethod.translateDate(group.getRegDtm()) : null);
		group.setModDtm(group.getModDtm() != null ? commonMethod.translateDate(group.getModDtm()) : null);
	}
	/**
	 * Transaction 결과 값을 반환 한다.
	 *
	 * @param result 결과값
	 * @return 결과값
	 */
	public <T> ResponseEntity<T> returnResultWhenTransaction(int result, Supplier<T> onSuccess) {

		if (result == 1) return ResponseEntity.ok(onSuccess.get());
		else if (result == 0) return ResponseEntity.notFound().build();
		else return ResponseEntity.badRequest().build();
	}

}
