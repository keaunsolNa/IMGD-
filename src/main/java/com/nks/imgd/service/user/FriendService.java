package com.nks.imgd.service.user;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.dto.schema.FriendTable;
import com.nks.imgd.mapper.user.FriendTableMapper;

@Service
public class FriendService {

	private final FriendTableMapper friendTableMapper;
	private final CommonMethod commonMethod = new CommonMethod();

	public FriendService(FriendTableMapper friendTableMapper) {
		this.friendTableMapper = friendTableMapper;
	}

	/**
	 * 유저 아이디로 대상 유저의 friend Id 가져오기
	 * @param userId 유저 아이디
	 * @return FRIEND_TABLE
	 */
	public FriendTable findFriendTableIdByUserId(@Param("userId") String userId) {
		return postProcessingFriendTable(friendTableMapper.findFriendTableIdByUserId(userId));
	}

	/**
	 * FRIEND_TABLE 목록 반환 시 후처리 진행 한다.
	 * DTM(YYYYMMDD) -> YYYY년 MM월 DD일
	 * @param friends 대상 FRIEND_TABLE 리스트
	 * @return 후처리 후 대상 FRIEND_TABLE 리스트
	 */
	public List<FriendTable> postProcessingFriendTables(List<FriendTable> friends) {

		for (FriendTable friend : friends) {
			postProcessingFriendTable(friend);
		}

		return friends;
	}

	/**
	 * Friend Table 반환 시 후처리 진행 한다.
	 * DTM(YYYYMMDD) -> YYYY년 MM월 DD일
	 * @param friend 대상 테이블
	 * @return 후처리 후 대상 FRIEND_TABLE
	 */
	public FriendTable postProcessingFriendTable(FriendTable friend) {

		friend.setRegDtm(null != friend.getRegDtm() ? commonMethod.translateDate(friend.getRegDtm()) : null);
		friend.setModDtm(null != friend.getModDtm() ? commonMethod.translateDate(friend.getModDtm()) : null);
		return friend;
	}
}
