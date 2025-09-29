package com.nks.imgd.controller.article;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.component.util.maker.ApiResponse;
import com.nks.imgd.dto.data.ArticleWithTagsAndFiles;
import com.nks.imgd.dto.searchdto.ArticleSearch;
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
	 * 조회 조건에 맞는 모든 게시글을 반환 한다.
	 * @param search 동적 쿼리에 맞는 검색 조건 목록
	 * @return 모든 게시글 데이터
	 */
	@GetMapping("/findAllArticle")
	public ResponseEntity<List<ArticleWithTagsAndFiles>> findAllArticle(@ModelAttribute ArticleSearch search) {
		return ResponseEntity.ok(articleService.findAllArticle(search));
	}

	/**
	 * 대상 게시글을 반환한다.
	 *
	 * @param articleId 대상 게시글 아이디
	 * @return 대상 게시글
	 */
	@GetMapping("/findArticleById")
	public ResponseEntity<ArticleWithTagsAndFiles> findArticleById(@AuthenticationPrincipal Jwt jwt,
		@RequestParam Long articleId) {
		return ResponseEntity.ok(articleService.findArticleById(articleId, jwt.getSubject()));
	}

	/**
	 * 신규 게시글을 추가 한다.
	 * 
	 * @param dto 추가할 게시글 정보
	 * @param jwt 사용자 정보
	 * @return 모든 게시글 데이터
	 */
	@PostMapping("/insertArticle")
	public ResponseEntity<ApiResponse<List<ArticleWithTagsAndFiles>>> insertArticle(@AuthenticationPrincipal Jwt jwt,
		@RequestBody ArticleWithTagsAndFiles dto) {
		dto.setUserId(jwt.getSubject());
		dto.setRegId(jwt.getSubject());

		return commonMethod.responseTransaction(articleService.insertArticle(dto));
	}

	/**
	 * 대상 게시글에 댓글을 추가한다.
	 * 
	 * @param jwt 사용자 정보
	 * @param dto 대상 댓글 정보
	 * @param articleId 댓글 다는 게시글 정보
	 * @return 해당 게시글 데이터
	 */
	@PostMapping("/insertComment")
	public ResponseEntity<ApiResponse<ArticleWithTagsAndFiles>> insertComment(@AuthenticationPrincipal Jwt jwt,
		@RequestBody ArticleWithTagsAndFiles dto, @RequestParam Long articleId) {
		dto.setUserId(jwt.getSubject());
		dto.setRegId(jwt.getSubject());

		return commonMethod.responseTransaction(articleService.insertComment(dto, articleId));
	}

	/**
	 * 게시글에 좋아요 표시
	 * 
	 * @param jwt 로그인한 대상 JWT 토큰 정보
	 * @param articleId 대상 게시글 아이디
	 * @return 좋아요한 게시글
	 */
	@PutMapping("/likeArticle")
	public ResponseEntity<ApiResponse<ArticleWithTagsAndFiles>> likeArticle(@AuthenticationPrincipal Jwt jwt,
		@RequestBody Long articleId) {
		return commonMethod.responseTransaction(articleService.likeArticle(articleId, jwt.getSubject()));
	}

	/**
	 * 게시글에 달린 댓글을 삭제한다.
	 * @param jwt 로그인 유저 토큰 정보
	 * @param articleId 게시글 정보
	 * @param commentId 댓글 정보
	 * @return 해당 게시글 정보
	 */
	@DeleteMapping("/deleteArticleComment")
	public ResponseEntity<ApiResponse<ArticleWithTagsAndFiles>> deleteArticleComment(@AuthenticationPrincipal Jwt jwt,
		@RequestParam Long articleId, @RequestParam Long commentId) {
		return commonMethod
			.responseTransaction(articleService.deleteArticleComment(articleId, commentId, jwt.getSubject()));
	}

}
