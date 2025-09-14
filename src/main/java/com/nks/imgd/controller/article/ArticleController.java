package com.nks.imgd.controller.article;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.component.util.maker.ApiResponse;
import com.nks.imgd.dto.dataDTO.ArticleWithTags;
import com.nks.imgd.service.article.ArticleService;

/**
 * @author nks
 * @apiNote Article Controller
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

	private final ArticleService articleService;
	private static final CommonMethod commonMethod = new CommonMethod();

	public ArticleController(ArticleService articleService) {
		this.articleService = articleService;
	}

	/**
	 * 모든 게시글을 반환 한다.
	 * 
	 * @return 모든 게시글 데이터
	 */
	@GetMapping("/findAllArticle")
	public ResponseEntity<List<ArticleWithTags>> findAllArticle() {
		return ResponseEntity.ok(articleService.findAllArticle());
	}

	/**
	 * 신규 게시글을 추가 한다.
	 * 
	 * @param dto 추가할 게시글 정보
	 * @return 모든 게시글 데이터
	 */
	@PostMapping("/insertArticle")
	public ResponseEntity<ApiResponse<List<ArticleWithTags>>> insertArticle( @AuthenticationPrincipal Jwt jwt, @RequestBody ArticleWithTags dto) {
		dto.setUserId(jwt.getSubject());
		dto.setRegId(jwt.getSubject());

		return commonMethod.responseTransaction(articleService.insertArticle(dto));
	}
}
