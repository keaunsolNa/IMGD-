package com.nks.imgd.controller.group;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nks.imgd.dto.GroupTableDTO;
import com.nks.imgd.service.group.GroupService;

@RestController
@RequestMapping("/group")
public class GroupController {

	private final GroupService groupService;

	public GroupController(GroupService groupService) {
		this.groupService = groupService;
	}

	/**
	 * 그룹을 생성한다.
	 * GroupTable 테이블 생성 후
	 * 생성 유저를 통해 GroupUser Table 도 생성한다.
	 *
	 * @param dto 그룹 생성 요청 DTO
	 * @return 생성 성공 여부
	 */
	@PostMapping(value = "/makeGroup")
	public ResponseEntity<String> createGroup(@RequestBody GroupTableDTO dto) {
		int inserted = groupService.makeNewGroup(dto);
		if (inserted > 0) {
			return ResponseEntity.ok("그룹 생성 완료");
		} else {
			return ResponseEntity.internalServerError().body("그룹 생성 실패");
		}
	}

	/**
	 * 생성된 그룹에 유저를 추가한다.
	 * 그룹원의 구성은 GroupTable 유지하고,
	 * GroupUser 테이블에 Row 추가하는 식으로 이루어진다.
	 * @param dto 대상 그룹 테이블
	 * @param userId 추가할 유저의 ID
	 * @return 생성 성공 여부
	 */
	@PostMapping(value = "/addGroupUser")
	public ResponseEntity<String> makeNewGroupUser(@RequestBody GroupTableDTO dto, @RequestParam String userId) {
		int inserted = groupService.makeNewGroupUser(dto, userId);
		if (inserted > 0) {
			return ResponseEntity.ok("그룹 유저 생성 완료");
		} else {
			return ResponseEntity.internalServerError().body("그룹 생성 실패");
		}
	}

	@DeleteMapping(value = "/deleteGroupUser")
	public ResponseEntity<String> deleteGroupUser(@RequestBody GroupTableDTO dto, @RequestParam String userId) {

		int inserted = groupService.deleteGroupUser(dto, userId);
		if (inserted == -1) {
			return ResponseEntity.ok("대상 유저가 그룹 내에 존재하지 않습니다.");
		}
		else if (inserted == -2) {
			return ResponseEntity.ok("그룹 마스터 유저일 경우 마스터 권한을 다른 유저에게 먼저 넘기셔야 합니다.");
		}
		else if (inserted > 0)
		{
			return ResponseEntity.ok("정상적으로 그룹이 삭제되었습니다.");
		}
		else
		{
			return ResponseEntity.internalServerError().body("그룹 계정 삭제 실패");
		}
	}

	@PostMapping(value = "/changeMstUserGroup")
	public ResponseEntity<String> changeMstUserGroup(@RequestBody GroupTableDTO dto, @RequestParam String userId) {

		int inserted = groupService.changeMstUserGroup(dto, userId);
		if (inserted == -1) {
			return ResponseEntity.ok("대상 유저가 그룹 내에 존재하지 않습니다.");
		}
		else if (inserted > 0)
		{
			return ResponseEntity.ok("Group 마스터 계정 변경 완료");
		}
		else
		{
			return ResponseEntity.internalServerError().body("그룹 계정 변경 실패");
		}
	}
}
