package com.nks.imgd.service.article;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.Enum.ArticleType;
import com.nks.imgd.dto.Enum.ResponseMsg;
import com.nks.imgd.dto.Schema.ArticleComment;
import com.nks.imgd.dto.Schema.ArticleLike;
import com.nks.imgd.dto.Schema.ArticleTag;
import com.nks.imgd.dto.Schema.Tag;
import com.nks.imgd.dto.dataDTO.ArticleWithTags;
import com.nks.imgd.dto.searchDTO.ArticleSearch;
import com.nks.imgd.mapper.article.ArticleMapper;
import com.nks.imgd.service.tag.TagService;

/**
 * @author nks
 * @apiNote Article Service
 */
@Service
public class ArticleService {

	private static final CommonMethod commonMethod = new CommonMethod();
	private final ArticleMapper articleMapper;
	private final ArticleTagService articleTagService;
	private final TagService tagService;
	private final ArticleLikeService articleLikeService;
	private final ArticleCommentService articleCommentService;

	public ArticleService(ArticleMapper articleMapper, ArticleTagService articleTagService, TagService tagService,
		ArticleLikeService articleLikeService, ArticleCommentService articleCommentService) {
		this.articleMapper = articleMapper;
		this.articleTagService = articleTagService;
		this.tagService = tagService;
		this.articleLikeService = articleLikeService;
		this.articleCommentService = articleCommentService;
	}

	/**
	 * 모든 게시글 목록 반환
	 *
	 * @return 모든 게시글 목록 반환
	 */
	public List<ArticleWithTags> findAllArticle(ArticleSearch search) {

		return postProcessingArticleTables(articleMapper.findAllArticle(search));
	}

	/**
	 * 아이디로 게시글 검색
	 * 게시글 작성자와 userId가 다를 경우 watch Count + 1
	 * @param articleId 대상 게시글 아이디
	 * @param userId api 호출한 유저 아이디
	 * @return 대상 게시글
	 */
	@Transactional(rollbackFor = Exception.class)
	public ArticleWithTags findArticleById(Long articleId, String userId) {

		if (!articleMapper.findArticleById(articleId).getUserId().equals(userId))
		{
			ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
				articleMapper.increaseArticleWatchCnt(articleId)
			);

			if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
				return null;
			}
		}

		return postProcessingArticleTable(articleMapper.findArticleById(articleId));
	}

	/**
	 * 신규 게시글 작성
	 * 게시글일 경우 게시글의 태그도 같이 ArticleTag에 등록한다.
	 * 
	 * @param dto 게시글 정보
	 * @return 모든 게시글 목록
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<List<ArticleWithTags>> insertArticle(ArticleWithTags dto) {

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			articleMapper.makeNewArticle(dto)
		);

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		if (dto.getType().equals(ArticleType.POST))
		{
			for (Tag tag : dto.getTagList())
			{
				ArticleTag articleTag = new ArticleTag();
				articleTag.setArticleId(dto.getArticleId());
				articleTag.setTagId(tag.getTagId());

				fsMsg = articleTagService.makeNewArticleTag(articleTag).status();

				if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
					return ServiceResult.failure(fsMsg);
				}
			}

		}

		return ServiceResult.success(() -> findAllArticle(new ArticleSearch()));
	}

	/**
	 * 신규 댓글 작성
	 * 
	 * @param dto 댓글 정보
	 * @param articleId 대상 게시글 정보   
	 * @return 대상 게시글 정보
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<ArticleWithTags> insertComment(ArticleWithTags dto, Long articleId) {

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			articleMapper.makeNewArticle(dto)
		);

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		if (dto.getType().equals(ArticleType.COMMENT))
		{
			ArticleComment articleComment = new ArticleComment();
			articleComment.setArticleId(articleId);
			articleComment.setCommentId(dto.getArticleId());

			System.out.println("articleComment : " + articleComment);
			fsMsg = articleCommentService.makeNewArticleComment(articleComment).status();

			if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
				return ServiceResult.failure(fsMsg);
			}
		}

		return ServiceResult.success(() -> findArticleById(articleId, dto.getUserId()));
	}
	
	/**
	 * 게시글에 좋아요 표시, 취소
	 * @param articleId 대상 게시글
	 * @param userId 좋아요 한 유저
	 * @return 대상 게시글 반환
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<ArticleWithTags> likeArticle(Long articleId, String userId) {

		System.out.println("likeArticle");
		if (articleMapper.findArticleById(articleId).getUserId().equals(userId))
			return ServiceResult.success(() -> findArticleById(articleId, userId));

		ServiceResult<ArticleLike> result;

		System.out.println("isLiked?");
		System.out.println(articleLikeService.isLiked(articleId, userId));

		if (articleLikeService.isLiked(articleId, userId))
		{
			result = articleLikeService.unLikeArticle(articleId, userId);
		}

		else
		{
			result = articleLikeService.likeArticle(articleId, userId);
		}

		System.out.println("IN LIKE");
		System.out.println(result.status());

		if (!result.status().equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(result.status());
		}

		return ServiceResult.success(() -> findArticleById(articleId, userId));
	}

	/**
	 * 게시글에 달린 댓글을 삭제한다.
	 * @param articleId 대상 게시글 id
	 * @param commentId 대상 댓글 아이디
	 * @param userId 유저 아이디
	 * @return 대상 게시글 반환
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<ArticleWithTags> deleteArticleComment(Long articleId, Long commentId, String userId) {

		ResponseMsg fsMsg = articleCommentService.deleteArticleComment(articleId, commentId).status();

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		fsMsg = commonMethod.returnResultByResponseMsg(
			articleMapper.deleteArticle(commentId)
		);

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		return ServiceResult.success(() -> findArticleById(articleId, userId));
	}

	// ───────────────────────────────── helper methods ───────────────────────────────

	public List<ArticleWithTags> postProcessingArticleTables(List<ArticleWithTags> articles) {

		for (ArticleWithTags article : articles)
		{
			postProcessingArticleTable(article);
		}
		return articles;
	}

	public ArticleWithTags postProcessingArticleTable(ArticleWithTags article) {

		article.setRegDtm(null != article.getRegDtm() ? commonMethod.translateDate(article.getRegDtm()) : null);
		article.setModDtm(null != article.getModDtm() ? commonMethod.translateDate(article.getModDtm()) : null);

		if (null != article.getTagIds())
		{
			List<Tag> tagList = new ArrayList<>();

			String[] tagIds = article.getTagIds().split(",");

			for (String tag : tagIds)
			{
				Tag tagDto = tagService.findTagById(Long.parseLong(tag));
				tagList.add(tagDto);
			}

			article.setTagList(tagList);
		}

		List<ArticleComment> comments = articleCommentService.findArticleCommentById(article.getArticleId());
		List<ArticleWithTags> commentArticles = new ArrayList<>();

		for (ArticleComment comment : comments)
		{
			Long commentId = comment.getCommentId();
			commentArticles.add(articleMapper.findArticleById(commentId));
		}

		article.setComments(commentArticles);

		return article;
	}


}
