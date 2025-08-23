package com.nks.imgd.controller.group;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.nks.imgd.dto.group.GroupTableDTO;
import com.nks.imgd.service.group.GroupService;

@RestController
@RequestMapping("/group")
public class GroupController {

	private final GroupService groupService;

	public GroupController(GroupService groupService) {
		this.groupService = groupService;
	}

	/**
	 * 로그인 한 유저가 가지고 있는 그룹 폴더가 없는 그룹을 확인한다.
	 *
	 * @param jwt JWT 로그인 되어 있는 권한
	 * @return 대상 유저가 가지고 있는 그룹 목록
	 */
	@GetMapping("/findGroupName")
	public ResponseEntity<List<GroupTableDTO>> findGroupName(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(groupService.findGroupName(jwt.getSubject()));
	}

	/**
	 * 로그인 한 유저가 가지고 있는 그룹을 확인한다.
	 *
	 * @param jwt JWT 로그인 되어 있는 권한
	 * @return 대상 유저가 가지고 있는 그룹 목록
	 */
	@GetMapping("/findGroupWhatInside")
	public ResponseEntity<List<GroupTableDTO>> findGroupWhatInside(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(groupService.findGroupWhatInside(jwt.getSubject()));
	}

	/**
	 * 그룹을 생성한다.
	 * GroupTable 테이블 생성 후
	 * 생성 유저를 통해 GroupUser Table 도 생성한다.
	 *
	 * @param dto 그룹 생성 요청 DTO
	 * @param jwt JWT 로그인 되어 있는 권한
	 * @return 생성 성공 여부
	 */
	@PostMapping("/createGroup")
	public ResponseEntity<String> createGroup(@RequestBody GroupTableDTO dto, @AuthenticationPrincipal Jwt jwt) {
		dto.setGroupMstUserId(jwt.getSubject());

		int inserted = groupService.makeNewGroup(dto);
		if (inserted > 0) {
			return ResponseEntity.ok("Complete make group.");
		} else {
			return ResponseEntity.internalServerError().body("Failed make group.");
		}
	}

	/**
	 * 생성된 그룹에 유저를 추가한다.
	 * 그룹원의 구성은 GroupTable 유지하고,
	 * GroupUser 테이블에 Row 추가하는 식으로 이루어진다.
	 * @param dto 대상 그룹 테이블
	 * @param userId 추가할 유저의 ID
	 * @param jwt JWT 로그인 되어 있는 권한
	 * @return 생성 성공 여부
	 */
	@PostMapping(value = "/makeNewGroupUser")
	public ResponseEntity<String> makeNewGroupUser(@RequestBody GroupTableDTO dto, @RequestParam String userId, @AuthenticationPrincipal Jwt jwt) {
		dto.setGroupMstUserId(jwt.getSubject());

		int inserted = groupService.makeNewGroupUser(dto, userId);
		if (inserted > 0) {
			return ResponseEntity.ok("Complete make group user.");
		} else {
			return ResponseEntity.internalServerError().body("Failed make group user.");
		}
	}

	@DeleteMapping(value = "/deleteGroupUser")
	public ResponseEntity<String> deleteGroupUser(@RequestBody GroupTableDTO dto, @RequestParam String userId, @AuthenticationPrincipal Jwt jwt) {
		dto.setGroupMstUserId(jwt.getSubject());

		int inserted = groupService.deleteGroupUser(dto, userId);
		if (inserted == -1) {
			return ResponseEntity.ok("Target user does not exist.");
		}
		else if (inserted == -2) {
			return ResponseEntity.ok("Group master user must transfer master rights to another user.");
		}
		else if (inserted > 0)
		{
			return ResponseEntity.ok("Complete delete group user.");
		}
		else
		{
			return ResponseEntity.internalServerError().body("Failed delete group user.");
		}
	}

	@PostMapping(value = "/changeMstUserGroup")
	public ResponseEntity<String> changeMstUserGroup(@RequestBody GroupTableDTO dto, @RequestParam String userId, @AuthenticationPrincipal Jwt jwt) {
		dto.setGroupMstUserId(jwt.getSubject());

		int inserted = groupService.changeMstUserGroup(dto, userId);
		if (inserted == -1) {
			return ResponseEntity.ok("Target user does not exist.");
		}
		else if (inserted > 0)
		{
			return ResponseEntity.ok("Complete group master user transfer.");
		}
		else
		{
			return ResponseEntity.internalServerError().body("Failed group master user transfer.");
		}
	}
}
