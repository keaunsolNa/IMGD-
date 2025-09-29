package com.nks.imgd.service.user;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.data.UserTableWithRelationshipAndPictureNmDto;
import com.nks.imgd.dto.enums.ResponseMsg;
import com.nks.imgd.mapper.user.UserFriendTableMapper;

@Service
public class UserFriendService {

	private final UserFriendTableMapper userFriendTableMapper;
	private final FriendService friendService;
	private final CommonMethod commonMethod = new CommonMethod();

	public UserFriendService(UserFriendTableMapper userFriendTableMapper, FriendService friendService) {
		this.userFriendTableMapper = userFriendTableMapper;
		this.friendService = friendService;
	}

	/**
	 * 친구 목록에 친구를 추가 한다.
	 * @param userId 로그인 한 유저 아이디
	 * @param targetUserId 추가 하려는 친구 유저 아이디
	 * @return 결과값 반환
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<List<UserTableWithRelationshipAndPictureNmDto>> insertUserFriendTable(
		@Param("userId") String userId, @Param("targetUserId") String targetUserId,
		@Param("relationship") String relationship) {

		Long friendId = friendService.findFriendTableIdByUserId(userId).getFriendId();

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			userFriendTableMapper.insertUserFriendTable(targetUserId, friendId, userId, relationship));

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		return ServiceResult.success(() -> userFriendTableMapper.findFriendEachOther(userId));
	}

	/**
	 * 친구 목록에서 대상 친구를 삭제 한다.
	 * @param userId 로그인 한 유저 아이디
	 * @param targetUserId 삭제 하려는 친구 유저 아이디
	 * @return 결과값 반환
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<List<UserTableWithRelationshipAndPictureNmDto>> deleteUserFriendTable(
		@Param("userId") String userId, @Param("targetUserId") String targetUserId) {

		Long friendId = friendService.findFriendTableIdByUserId(userId).getFriendId();

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			userFriendTableMapper.deleteUserFriendTable(targetUserId, friendId));

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		return ServiceResult.success(() -> userFriendTableMapper.findFriendEachOther(userId));

	}
}
