package com.nks.imgd.service.article;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.Enum.ResponseMsg;
import com.nks.imgd.dto.Schema.ArticleTag;
import com.nks.imgd.dto.Schema.Tag;
import com.nks.imgd.dto.dataDTO.ArticleWithTags;
import com.nks.imgd.mapper.article.ArticleMapper;
import com.nks.imgd.service.articleTag.ArticleTagService;

/**
 * @author nks
 * @apiNote Article Service
 */
@Service
public class ArticleService {

	private static final CommonMethod commonMethod = new CommonMethod();
	private final ArticleMapper articleMapper;
	private final ArticleTagService articleTagService;

	public ArticleService(ArticleMapper articleMapper, ArticleTagService articleTagService) {
		this.articleMapper = articleMapper;
		this.articleTagService = articleTagService;
	}

	/**
	 * 모든 게시글 목록 반환
	 *
	 * @return 모든 게시글 목록 반환
	 */
	public List<ArticleWithTags> findAllArticle() {

		return postProcessingArticleTables(articleMapper.findAllArticle());
	}

	/**
	 * 신규 게시글 작성
	 * 게시글의 태그도 같이 ArticleTag에 등록한다.
	 * 
	 * @param dto 게시글 정보
	 * @return 모든 게시글 목록
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<List<ArticleWithTags>> insertArticle(ArticleWithTags dto) {

		System.out.println(dto);

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			articleMapper.makeNewArticle(dto)
		);

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		System.out.println("AFTER");
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

		System.out.println("AFTER2");
		return ServiceResult.success(this::findAllArticle);
	}

	// ───────────────────────────────── helper methods ───────────────────────────────

	public List<ArticleWithTags> postProcessingArticleTables(List<ArticleWithTags> articles) {

		for (ArticleWithTags article : articles)
		{
			postProcessingArticleTable(article);
		}
		return articles;
	}

	public void postProcessingArticleTable(ArticleWithTags article) {

		article.setRegDtm(null != article.getRegDtm() ? commonMethod.translateDate(article.getRegDtm()) : null);
		article.setModDtm(null != article.getModDtm() ? commonMethod.translateDate(article.getModDtm()) : null);
	}


}
