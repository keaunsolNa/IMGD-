package com.nks.imgd.service.article;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.Enum.ResponseMsg;
import com.nks.imgd.dto.Schema.ArticleComment;
import com.nks.imgd.dto.dataDTO.ArticleWithTags;
import com.nks.imgd.mapper.article.ArticleCommentMapper;

/**
 * @author nks
 * @apiNote ArticleComment Service
 */
@Service
public class ArticleCommentService {

	private static final CommonMethod commonMethod = new CommonMethod();
	private final ArticleCommentMapper articleCommentMapper;

	public ArticleCommentService(ArticleCommentMapper articleCommentMapper) {
		this.articleCommentMapper = articleCommentMapper;
	}

	/**
	 * articleId, tagId 와 일치하는 대상을 반환한다.
	 *
	 * @param articleId 게시글 아이디
	 * @param commentId 댓글 아이디
	 * @return 대상 정보
	 */
	public ArticleComment findArticleTagById(Long articleId, Long commentId) {
		return articleCommentMapper.findArticleCommentByIds(articleId, commentId);
	}

	/**
	 * articleId에 달린 commentId(articleId)를 반환한다.
	 * 
	 * @param articleId 대상 게시글 아이디
	 * @return 대상 댓글(Article) 아이디 목록
	 */
	public List<ArticleComment> findArticleCommentById(Long articleId) {
		return articleCommentMapper.findArticleCommentById(articleId);
	}


	/**
	 * 게시글-댓글 테이블 입력한다.
	 * 
	 * @param articleComment 대상 정보
	 * @return 대상 게시글 오브젝트
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<ArticleComment> makeNewArticleComment(ArticleComment articleComment) {

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			articleCommentMapper.insertArticleComment(articleComment)
		);

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		return ServiceResult.success(() -> findArticleTagById(articleComment.getArticleId(), articleComment.getCommentId()));
	}

	/**
	 * 게시글 - 댓글 테이블 삭제한다.
	 *
	 * @param articleId 대상 게시글 아이디
	 * @param commentId 대상 댓글 아이디
	 * @return boolean
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<ArticleWithTags> deleteArticleComment(Long articleId, Long commentId) {

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			articleCommentMapper.deleteArticleComment(articleId, commentId)
		);

		if(!fsMsg.equals(ResponseMsg.ON_SUCCESS)) return ServiceResult.failure(fsMsg);

		return ServiceResult.success(() -> articleCommentMapper.findArticleById(articleId));
	}
}
