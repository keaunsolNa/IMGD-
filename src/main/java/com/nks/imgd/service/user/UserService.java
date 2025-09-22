package com.nks.imgd.service.user;

import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.Enum.ResponseMsg;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.dto.Schema.FileTable;
import com.nks.imgd.dto.dataDTO.GroupTableWithMstUserNameDTO;
import com.nks.imgd.dto.dataDTO.UserTableWithRelationshipAndPictureNmDTO;
import com.nks.imgd.mapper.user.UserTableMapper;
import com.nks.imgd.service.file.FileService;
import com.nks.imgd.service.group.GroupService;

import java.util.List;

@Service
public class UserService {

	private final UserTableMapper userTableMapper;
	private final FileService fileService;
	private final CommonMethod commonMethod = new CommonMethod();
	private final GroupService groupService;

	public UserService(UserTableMapper userTableMapper, FileService fileService, GroupService groupService)
	{
		this.userTableMapper = userTableMapper;
		this.fileService = fileService;
		this.groupService = groupService;
	}

	/**
	 * 로그인 한 유저가 가지고 있는 회원 정보를 반환 한다.
	 *
	 * @param userId 대상 유저 ID
	 * @return 대상 유저가 가지고 있는 정보
	 */
	public UserTableWithRelationshipAndPictureNmDTO findUserById(@Param("userId") String userId) {

		return postProcessingUserTable(userTableMapper.findById(userId));
	}

	/**
	 * 대상 유저와 서로 친구 관계인 유저 목록을 가져 온다.
	 * 반환 정보는 ID, NAME, PICTURE_ID, REG_DTM (친구 맺은 시기)
	 * @param userId 대상 유저 아이디
	 * @return 친구 목록
	 */
	public List<UserTableWithRelationshipAndPictureNmDTO> findFriendEachOther(@Param("userId") String userId) {
		return postProcessingUserTables(userTableMapper.findFriendEachOther(userId));
	}

	/**
	 * 대상 유저는 친구로 추가 했지만, 본인은 추가 하지 않은 유저 목록을 가져 온다.
	 * @param userId 대상 유저 아이디
	 * @return 대상 목록
	 */
	public List<UserTableWithRelationshipAndPictureNmDTO> findFriendWhoAddMeButImNot(@Param("userId") String userId) {
		return postProcessingUserTables(userTableMapper.findFriendWhoAddMeButImNot(userId));
	}

	/**
	 * 친구 신청을 했지만, 상대는 수락 하지 않은 유저 목록을 가져 온다.
	 * @param userId 대상 유저 아이디
	 * @return 대상 목록
	 */
	public List<UserTableWithRelationshipAndPictureNmDTO> findFriendWhoImAddButNot(@Param("userId") String userId) {
		return postProcessingUserTables(userTableMapper.findFriendWhoImAddButNot(userId));
	}

	/**
	 * 친구 신청을 했지만, 상대가 거절한 유저 목록을 가져 온다.
	 * @param userId 대상 유저 아이디
	 * @return 대상 목록
	 */
	public List<UserTableWithRelationshipAndPictureNmDTO> findFriendWhoImAddButReject(@Param("userId") String userId) {
		return postProcessingUserTables(userTableMapper.findFriendWhoImAddButReject(userId));
	}

	/**
	 * 내가 등록한 친구 목록을 가져 온다.
	 * @param userId 대상 유저 아이디
	 * @return 대상 목록
	 */
	public List<UserTableWithRelationshipAndPictureNmDTO> findFriend(@Param("userId") String userId){
		return postProcessingUserTables(userTableMapper.findFriend(userId));
	}

	/**
	 * 대상과 나와의 관계를 검색한다.
	 * @param userId 대상 유저 아이디
	 * @return 대상 목록
	 */
	public UserTableWithRelationshipAndPictureNmDTO searchFriend(@Param("loginId") String loginId, @Param("userId") String userId){
		return postProcessingUserTable(userTableMapper.searchFriend(loginId, userId));
	}


	/**
	 * 해당 그룹에 소속 되지 않은 친구 목록을 가져 온다.
	 * @param userId 대상 유자 아이디
	 * @param groupId 대상 그룹 아이디
	 * @return 대상 유저 목록
	 */
	public List<UserTableWithRelationshipAndPictureNmDTO> findFriendEachOtherAndNotInGroup(@Param("userId") String userId, @Param("groupId") Long groupId){
		return postProcessingUserTables(userTableMapper.findFriendEachOtherAndNotInGroup(userId, groupId));
	}
	/**
	 * 로그인 한 유저가 가지고 있는 회원 정보를 변경 한다.
	 *
	 * @param user 대상 유저 DTO
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<UserTableWithRelationshipAndPictureNmDTO> updateUser(@Param("user") UserTableWithRelationshipAndPictureNmDTO user) {

        ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
                userTableMapper.updateUser(user)
        );

        if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
            return ServiceResult.failure(fsMsg);
        }

        return ServiceResult.success(() -> findUserById(user.getUserId()));
	}

	/**
	 * 계정을 제거한다.
	 * 
	 * @param userId 제거하려는 계정 아이디
	 * @return 제거 결과 반환
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<ResponseMsg> deleteUser(@Param("userId") String userId) {

		// 그룹 관련 삭제
		List<GroupTableWithMstUserNameDTO> groups = groupService.findGroupWhatUserIsMstAndJustOnlyOne(userId);

		for (GroupTableWithMstUserNameDTO group : groups) {
			groupService.deleteGroup(userId, group.getGroupId());
		}
		
		// TODO 게시글 관련 삭제

		// 유저 프로필 사진 삭제
		FileTable file = fileService.findUserProfileFileId(userId);

		if (null != file)
		{
			if (!fileService.deleteFileById(file.getFileId())) return ServiceResult.failure(ResponseMsg.FILE_DELETE_FAILED);
		}

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			userTableMapper.deleteUser(userId)
		);

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		return ServiceResult.success(() -> fsMsg);
	}

	// ───────────────────────────────── helper methods ───────────────────────────────

	/**
	 * 유저 목록 반환 시 후처리 진행 한다.
	 * DTM(YYYYMMDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 * @param users 대상 유저 리스트
	 * @return 후처리 후 대상 유저 리스트
	 */
	public List<UserTableWithRelationshipAndPictureNmDTO> postProcessingUserTables(List<UserTableWithRelationshipAndPictureNmDTO> users) {

		for (UserTableWithRelationshipAndPictureNmDTO user : users) {
			postProcessingUserTable(user);
		}

		return users;
	}

	/**
	 * 유저 반환 시 후처리 진행 한다.
	 * DTM(YYYYMMDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 * @param user 대상 유저
	 * @return 후처리 후 대상 유저
	 */
	public UserTableWithRelationshipAndPictureNmDTO postProcessingUserTable(
		UserTableWithRelationshipAndPictureNmDTO user) {

		if (null == user) return null;
		user.setLastLoginDate(null != user.getLastLoginDate()  ? commonMethod.translateDate(user.getLastLoginDate()) : null);
		user.setRegDtm(null != user.getRegDtm() ? commonMethod.translateDate(user.getRegDtm()) : null);
		user.setModDtm(null != user.getModDtm() ? commonMethod.translateDate(user.getModDtm()) : null);

		return user;
	}

}
