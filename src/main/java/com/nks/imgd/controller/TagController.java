package com.nks.imgd.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.component.util.maker.ApiResponse;
import com.nks.imgd.dto.Schema.Tag;
import com.nks.imgd.service.tag.TagService;

/**
 * @author nks
 * @apiNote Tag Controller
 */
@RestController
@RequestMapping("/tag")
public class TagController {

	private final TagService tagService;
	private static final CommonMethod commonMethod = new CommonMethod();

	public TagController(TagService tagService) {
		this.tagService = tagService;
	}

	/**
	 * 모든 태그 목록을 가져온다.
	 * @return 모든 태그 목록
	 */
	@GetMapping("/findAllTag")
	public ResponseEntity<List<Tag>> findAllTag() {
		return ResponseEntity.ok(tagService.findAllTag());
	}

	/**
	 * 대상 이름과 유사한 태그 목록을 반환한다.
	 * @param name 대상 태그 이름
	 * @return 유사한 태그 목록
	 */
	@GetMapping("/selectTagByLikeName")
	public ResponseEntity<List<Tag>> selectTagByLikeName(@RequestParam("name") String name) {
		return ResponseEntity.ok(tagService.selectTagByLikeName(name));
	}

	/**
	 * 신규 태그 생성
	 * @param jwt 로그인 한 유저 권한
	 * @param tag 생성할 태그 정보
	 * @return 모든 태그 목록
	 */
	@PostMapping("/makeNewTag")
	public ResponseEntity<ApiResponse<List<Tag>>> makeNewTag(@AuthenticationPrincipal Jwt jwt, @RequestBody Tag tag) {
		return commonMethod.responseTransaction(tagService.insertTagList(jwt.getSubject(), tag));
	}
}
