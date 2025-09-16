package com.nks.imgd.service.articleLike;

import org.springframework.stereotype.Service;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.Enum.ResponseMsg;
import com.nks.imgd.dto.Schema.ArticleLike;
import com.nks.imgd.mapper.articleLike.ArticleLikeMapper;

/**
 * @author nks
 * @apiNote ArticleLike Service
 */
@Service
public class ArticleLikeService {

	private static final CommonMethod commonMethod = new CommonMethod();
	private final ArticleLikeMapper articleLikeMapper;

	public ArticleLikeService(ArticleLikeMapper articleLikeMapper) {
		this.articleLikeMapper = articleLikeMapper;
	}

	/**
	 * 대상 게시글에 Like 했는지 확인
	 *
	 * @param articleId 대상 게시글 아이디
	 * @param userId 대상 유저 아이디
	 * @return boolean 값, Like 했다면 true / 아니라면 false
	 */
	public boolean isLiked(Long articleId, String userId) {
		return null != articleLikeMapper.findArticleLikeWhatIds(articleId, userId);
	}

	/**
	 * 대상 게시글에 Like 했는지 확인 후 대상 정보 반환
	 *
	 * @param articleId 대상 게시글 아이디
	 * @param userId 대상 유저 아이디
	 * @return 대상 게시글/유저 정보
	 */
	public ArticleLike findArticleLikeWhatIds (Long articleId, String userId) {
		return articleLikeMapper.findArticleLikeWhatIds(articleId, userId);
	}

	/**
	 * 게시글에 좋아요 표시 (INSERT)
	 * 
	 * @param articleId 대상 게시글 아이디
	 * @param userId 대상 유저 아이디
	 * @return 대상 결과
	 */
	public ServiceResult<ArticleLike> likeArticle (Long articleId, String userId) {

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			articleLikeMapper.likeArticle(articleId, userId)
		);

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		return ServiceResult.success(() -> findArticleLikeWhatIds(articleId, userId));
	}

	/**
	 * 게시글 좋아요 표시 취소 (DELETE)
	 * 
	 * @param articleId 대상 게시글 아이디
	 * @param userId 대상 유저 아이디
	 * @return 대상 결과
	 */
	public ServiceResult<ArticleLike> unLikeArticle (Long articleId, String userId) {

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			articleLikeMapper.unLikeArticle(articleId, userId)
		);

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		return ServiceResult.success(() -> findArticleLikeWhatIds(articleId, userId));
	}
}
