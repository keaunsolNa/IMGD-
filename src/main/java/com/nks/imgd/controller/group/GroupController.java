package com.nks.imgd.controller.group;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	 *
	 * @param dto 그룹 생성 요청 DTO
	 * @return 생성 성공 여부
	 */
	@PostMapping
	public ResponseEntity<String> createGroup(@RequestBody GroupTableDTO dto) {
		int inserted = groupService.makeGroup(dto);
		if (inserted > 0) {
			return ResponseEntity.ok("그룹 생성 완료");
		} else {
			return ResponseEntity.internalServerError().body("그룹 생성 실패");
		}
	}

}
