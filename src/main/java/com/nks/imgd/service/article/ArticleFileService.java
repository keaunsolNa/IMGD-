package com.nks.imgd.service.article;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.enums.ResponseMsg;
import com.nks.imgd.mapper.article.ArticleFileMapper;

@Service
public class ArticleFileService {

	private final ArticleFileMapper articleFileMapper;
	private static final CommonMethod commonMethod = new CommonMethod();

	public ArticleFileService(ArticleFileMapper articleFileMapper) {
		this.articleFileMapper = articleFileMapper;
	}

	/**
	 * 게시글에 파일 업로드 시 수행한다.
	 * 
	 * @param articleId 대상 게시글 아이디
	 * @param fileId 대상 파일 아이디
	 * @return 결과값
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<ResponseMsg> makeArticleFile(Long articleId, Long fileId) {
		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			articleFileMapper.makeArticleFile(articleId, fileId));

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		return ServiceResult.success(() -> fsMsg);
	}
}
