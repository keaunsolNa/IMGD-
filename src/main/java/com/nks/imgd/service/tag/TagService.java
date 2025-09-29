package com.nks.imgd.service.tag;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.enums.ResponseMsg;
import com.nks.imgd.dto.schema.Tag;
import com.nks.imgd.mapper.tag.TagMapper;

@Service
public class TagService {

	private static final CommonMethod commonMethod = new CommonMethod();
	private final TagMapper tagMapper;

	public TagService(TagMapper tagMapper) {
		this.tagMapper = tagMapper;
	}

	/**
	 * 모든 태그 목록 반환
	 * 
	 * @return 모든 태그 목록
	 */
	public List<Tag> findAllTag() {
		return postProcessingTagTables(tagMapper.findAllTag());
	}

	/**
	 * 아이디로 태그 검색
	 * @param tagId 대상 태그 아이디
	 * @return 태그 목록 반환
	 */
	public Tag findTagById(Long tagId) {
		return postProcessingTagTable(tagMapper.findTagById(tagId));
	}

	/**
	 * 태그 이름으로 Like 검색 시행
	 *
	 * @param name 대상 태그 이름
	 * @return 유사한 태그 목록 반환
	 *
	 */
	public List<Tag> selectTagByLikeName(String name) {
		return postProcessingTagTables(tagMapper.selectTagByLikeName(name));
	}

	/**
	 * 신규 태그 생성
	 *
	 * @param userId 생성하려는 유저 아이디
	 * @param tag 생성하려는 태그 정보
	 * @return 모든 태그 목록
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<List<Tag>> insertTagList(String userId, Tag tag) {

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			tagMapper.makeNewTag(userId, tag));

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		return ServiceResult.success(this::findAllTag);
	}

	// ───────────────────────────────── helper methods ───────────────────────────────

	public List<Tag> postProcessingTagTables(List<Tag> tags) {

		for (Tag tag : tags) {
			postProcessingTagTable(tag);
		}
		return tags;
	}

	public Tag postProcessingTagTable(Tag tag) {

		tag.setRegDtm(null != tag.getRegDtm() ? commonMethod.translateDate(tag.getRegDtm()) : null);
		tag.setModDtm(null != tag.getModDtm() ? commonMethod.translateDate(tag.getModDtm()) : null);

		return tag;
	}
}
