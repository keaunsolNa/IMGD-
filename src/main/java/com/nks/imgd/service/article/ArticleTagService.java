package com.nks.imgd.service.article;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.Enum.ResponseMsg;
import com.nks.imgd.dto.Schema.ArticleTag;
import com.nks.imgd.mapper.article.ArticleTagMapper;

/**
 * @author nks
 * @apiNote ArticleTag Service
 */
@Service
public class ArticleTagService {

	private static final CommonMethod commonMethod = new CommonMethod();

	private final ArticleTagMapper articleTagMapper;

	public ArticleTagService(ArticleTagMapper articleTagMapper) {
		this.articleTagMapper = articleTagMapper;
	}

	/**
	 * articleId, tagId 와 일치하는 대상을 반환한다.
	 *
	 * @param articleId 게시글 아이디
	 * @param tagId 태그 아이디
	 * @return 대상 정보
	 */
	public ArticleTag findArticleTagById(Long articleId, Long tagId) {
		return articleTagMapper.findArticleTagById(articleId, tagId);
	}

	/**
	 * 게시글-태그 테이블에 입력한다.
	 *
	 * @param articleTag 게시글-태그 정보
	 * @return 대상 게시글-테그 오브젝트
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<ArticleTag> makeNewArticleTag(ArticleTag articleTag) {

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			articleTagMapper.makeNewArticleTag(articleTag)
		);

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		return ServiceResult.success(() -> findArticleTagById(articleTag.getArticleId(), articleTag.getTagId()));
	}


	// ───────────────────────────────── helper methods ───────────────────────────────


}
