package com.nks.imgd.controller.group;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.component.util.maker.ApiResponse;
import com.nks.imgd.dto.data.GroupTableWithMstUserNameDto;
import com.nks.imgd.dto.data.GroupUserWithNameDto;
import com.nks.imgd.service.group.GroupService;

@RestController
@RequestMapping("/group/user")
public class GroupUserController {

	private final GroupService groupService;
	private static final CommonMethod commonMethod = new CommonMethod();

	public GroupUserController(GroupService groupService) {
		this.groupService = groupService;
	}

	/**
	 * 대상 그룹이 가지고 있는 유저 목록을 반환 한다.
	 *
	 * @param groupId 대상 그룹의 아이디
	 * @return 대상 유저가 가지고 있는 그룹 목록
	 */
	@GetMapping()
	public ResponseEntity<List<GroupUserWithNameDto>> findGroupUserWhatInside(@AuthenticationPrincipal Jwt jwt,
		@RequestParam Long groupId) {
		return ResponseEntity.ok(groupService.findGroupUserWhatInside(jwt.getSubject(), groupId));
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
	@PostMapping()
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
	@DeleteMapping()
	public ResponseEntity<ApiResponse<List<GroupUserWithNameDto>>> deleteGroupUser(
		@RequestBody GroupTableWithMstUserNameDto dto, @RequestParam String userId, @AuthenticationPrincipal Jwt jwt) {

		dto.setGroupMstUserId(jwt.getSubject());
		return commonMethod.responseTransaction(groupService.deleteGroupUser(dto, userId));
	}
}
