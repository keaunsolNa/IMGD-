package com.nks.imgd.controller.article;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.component.util.maker.ApiResponse;
import com.nks.imgd.dto.data.ArticleWithTagsAndFiles;
import com.nks.imgd.service.article.ArticleService;

@RestController
@RequestMapping("/article/comment")
public class ArticleCommentController {

	private final ArticleService articleService;
	private static final CommonMethod commonMethod = new CommonMethod();

	public ArticleCommentController(ArticleService articleService) {
		this.articleService = articleService;
	}

	/**
	 * 대상 게시글에 댓글을 추가한다.
	 *
	 * @param jwt 사용자 정보
	 * @param dto 대상 댓글 정보
	 * @param articleId 댓글 다는 게시글 정보
	 * @return 해당 게시글 데이터
	 */
	@PostMapping()
	public ResponseEntity<ApiResponse<ArticleWithTagsAndFiles>> insertComment(@AuthenticationPrincipal Jwt jwt,
		@RequestBody ArticleWithTagsAndFiles dto, @RequestParam Long articleId) {
		dto.setUserId(jwt.getSubject());
		dto.setRegId(jwt.getSubject());

		return commonMethod.responseTransaction(articleService.insertComment(dto, articleId));
	}

	/**
	 * 게시글에 달린 댓글을 삭제한다.
	 * @param jwt 로그인 유저 토큰 정보
	 * @param articleId 게시글 정보
	 * @param commentId 댓글 정보
	 * @return 해당 게시글 정보
	 */
	@DeleteMapping()
	public ResponseEntity<ApiResponse<ArticleWithTagsAndFiles>> deleteArticleComment(@AuthenticationPrincipal Jwt jwt,
		@RequestParam Long articleId, @RequestParam Long commentId) {
		return commonMethod
			.responseTransaction(articleService.deleteArticleComment(articleId, commentId, jwt.getSubject()));
	}

	/**
	 * 댓글 좋아요 표시
	 *
	 * @param jwt 로그인한 대상 JWT 토큰 정보
	 * @param articleId 대상 게시글 아이디
	 * @return 좋아요한 게시글
	 */
	@PutMapping()
	public ResponseEntity<ApiResponse<ArticleWithTagsAndFiles>> likeArticle(@AuthenticationPrincipal Jwt jwt,
		@RequestBody Long articleId) {
		return commonMethod.responseTransaction(articleService.likeArticle(articleId, jwt.getSubject()));
	}
}
