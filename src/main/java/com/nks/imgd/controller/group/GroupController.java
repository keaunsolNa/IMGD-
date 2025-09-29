package com.nks.imgd.controller.group;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.component.util.maker.ApiResponse;
import com.nks.imgd.dto.data.GroupTableWithMstUserNameDto;
import com.nks.imgd.dto.data.GroupUserWithNameDto;
import com.nks.imgd.service.group.GroupService;

@RestController
@RequestMapping("/group")
public class GroupController {

	private final GroupService groupService;
	private static final CommonMethod commonMethod = new CommonMethod();

	public GroupController(GroupService groupService) {
		this.groupService = groupService;
	}

	/**
	 * 로그인 한 유저가 가지고 있는 그룹 폴더가 없는 그룹을 반환 한다.
	 *
	 * @param jwt JWT 로그인 되어 있는 권한
	 * @return 대상 유저가 가지고 있는 그룹 목록
	 */
	@GetMapping("/findGroupName")
	public ResponseEntity<List<GroupTableWithMstUserNameDto>> findGroupName(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(groupService.findGroupName(jwt.getSubject()));
	}

	/**
	 * 로그인 한 유저가 가지고 있는 그룹을 반환 한다.
	 *
	 * @param jwt JWT 로그인 되어 있는 권한
	 * @return 대상 유저가 가지고 있는 그룹 목록
	 */
	@GetMapping("/findGroupWhatInside")
	public ResponseEntity<List<GroupTableWithMstUserNameDto>> findGroupWhatInside(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(groupService.findGroupWhatInside(jwt.getSubject()));
	}

	/**
	 * 대상 그룹이 가지고 있는 유저 목록을 반환 한다.
	 *
	 * @param groupId 대상 그룹의 아이디
	 * @return 대상 유저가 가지고 있는 그룹 목록
	 */
	@GetMapping("/findGroupUserWhatInside")
	public ResponseEntity<List<GroupUserWithNameDto>> findGroupUserWhatInside(@AuthenticationPrincipal Jwt jwt,
		@RequestParam Long groupId) {
		return ResponseEntity.ok(groupService.findGroupUserWhatInside(jwt.getSubject(), groupId));
	}

	/**
	 * 그룹을 생성 한다.
	 * GroupTable 테이블 생성 후
	 * 생성 유저를 통해 GroupUser Table 도 생성 한다.
	 *
	 * @param dto 그룹 생성 요청 DTO
	 * @param jwt JWT 로그인 되어 있는 권한
	 * @return 생성 된 그룹의 인원
	 */
	@PostMapping("/createGroup")
	public ResponseEntity<ApiResponse<GroupTableWithMstUserNameDto>> createGroup(
		@RequestBody GroupTableWithMstUserNameDto dto, @AuthenticationPrincipal Jwt jwt) {

		dto.setGroupMstUserId(jwt.getSubject());
		return commonMethod.responseTransaction(groupService.createGroup(dto));
	}

	/**
	 * 생성된 그룹에 유저를 추가 한다.
	 * 그룹원 구성은 GroupTable 유지,
	 * GroupUser 테이블 Row 추가 하는 식으로 수행.
	 * @param dto 대상 그룹 테이블
	 * @param userId 추가할 유저의 ID
	 * @param jwt JWT 로그인 되어 있는 권한
	 * @return 그룹 유저 목록
	 */
	@PostMapping(value = "/makeNewGroupUser")
	public ResponseEntity<ApiResponse<List<GroupUserWithNameDto>>> makeNewGroupUser(
		@RequestBody GroupTableWithMstUserNameDto dto, @RequestParam String userId, @AuthenticationPrincipal Jwt jwt) {

		dto.setGroupMstUserId(jwt.getSubject());
		return commonMethod.responseTransaction(groupService.makeNewGroupUser(dto, userId));
	}

	/**
	 * 그룹에서 해당 유저를 제거 한다.
	 * @param dto 대상 그룹 테이블
	 * @param userId 제거할 유저의 ID
	 * @param jwt JWT 로그인 되어 있는 권한
	 * @return 해당 그룹 정보
	 */
	@DeleteMapping(value = "/deleteGroupUser")
	public ResponseEntity<ApiResponse<List<GroupUserWithNameDto>>> deleteGroupUser(
		@RequestBody GroupTableWithMstUserNameDto dto, @RequestParam String userId, @AuthenticationPrincipal Jwt jwt) {

		dto.setGroupMstUserId(jwt.getSubject());
		return commonMethod.responseTransaction(groupService.deleteGroupUser(dto, userId));
	}

	/**
	 * 그룹에서 MST_USER_ID 를 변경 한다.
	 * @param dto 대상 그룹 테이블
	 * @param userId 제거할 유저의 ID
	 * @param jwt JWT 로그인 되어 있는 권한
	 * @return 해당 그룹 정보
	 */
	@PostMapping(value = "/changeMstUserGroup")
	public ResponseEntity<ApiResponse<List<GroupUserWithNameDto>>> changeMstUserGroup(
		@RequestBody GroupTableWithMstUserNameDto dto, @RequestParam String userId, @AuthenticationPrincipal Jwt jwt) {

		dto.setGroupMstUserId(jwt.getSubject());
		return commonMethod.responseTransaction(groupService.changeMstUserGroup(dto, userId));
	}

	/**
	 * 그룹을 삭제한다.
	 * 그룹 삭제 시 해당 그룹에 있는 모든 파일 / 폴더가 다 삭제된다.
	 * @param jwt 토큰 정보
	 * @param groupId 대상 그룹 정보
	 * @return 삭제 후 해당 유저가 가진 그룹 목록
	 */
	@DeleteMapping("/deleteGroup")
	public ResponseEntity<ApiResponse<List<GroupUserWithNameDto>>> deleteGroup(@AuthenticationPrincipal Jwt jwt,
		@RequestParam Long groupId) {
		return commonMethod.responseTransaction(groupService.deleteGroup(jwt.getSubject(), groupId));
	}

}
