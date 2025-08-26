package com.nks.imgd.service.user;

import org.apache.ibatis.annotations.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.dto.user.UserTableDTO;
import com.nks.imgd.mapper.user.UserTableMapper;

import java.util.List;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

	private final UserTableMapper userTableMapper;
	private final CommonMethod commonMethod = new CommonMethod();

	public UserService(UserTableMapper userTableMapper)
	{
		this.userTableMapper = userTableMapper;
	}

	/**
	 * 로그인 한 유저가 가지고 있는 회원 정보를 반환 한다.
	 *
	 * @param userId 대상 유저 ID
	 * @return 대상 유저가 가지고 있는 정보
	 */
	public UserTableDTO findUserById(@Param("userId") String userId) {

		return postProcessingUserTable(userTableMapper.findById(userId));
	}

	/**
	 * 대상 유저와 서로 친구 관계인 유저 목록을 가져 온다.
	 * 반환 정보는 ID, NAME, PICTURE_ID, REG_DTM (친구 맺은 시기)
	 * @param userId 대상 유저 아이디
	 * @return 친구 목록
	 */
	public List<UserTableDTO> findFriendEachOther(@Param("userId") String userId) {
		return postProcessingUserTables(userTableMapper.findFriendEachOther(userId));
	}

	/**
	 * 대상 유저를 친구로 추가 했지만, 대상 유저는 추가 하지 않은 유저 목록을 가져 온다.
	 * @param userId 대상 유저 아이디
	 * @return 대상 목록
	 */
	public List<UserTableDTO> findFriendWhoAddMeButImNot(@Param("userId") String userId) {
		return postProcessingUserTables(userTableMapper.findFriendWhoAddMeButImNot(userId));
	}

	/**
	 * 로그인 한 유저가 가지고 있는 회원 정보를 변경 한다.
	 *
	 * @param user 대상 유저 DTO
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseEntity<UserTableDTO>  updateUser(@Param("user") UserTableDTO user) {
		return returnResultWhenTransaction(userTableMapper.updateUser(user), () -> findUserById(user.getUserId()));
	}

	/**
	 * 친구 목록에 친구를 추가 한다.
	 * @param userId 로그인 한 유저 아이디
	 * @param targetUserId 추가 하려는 친구 유저 아이디
	 * @return 결과값 반환
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseEntity<List<UserTableDTO>> insertUserFriendTable(@Param("userId") String userId, @Param("targetUserId") String targetUserId) {

		Long friendId = userTableMapper.findFriendTableIdByUserId(userId).getFriendId();
		return returnResultWhenTransaction(userTableMapper.insertUserFriendTable(targetUserId, friendId), () -> findFriendEachOther(userId));
	}

	/**
	 * 친구 목록에서 대상 친구를 삭제 한다.
	 * @param userId 로그인 한 유저 아이디
	 * @param targetUserId 삭제 하려는 친구 유저 아이디
	 * @return 결과값 반환
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseEntity<List<UserTableDTO>> deleteUserFriendTable(@Param("userId") String userId, @Param("targetUserId") String targetUserId) {

		Long friendId = userTableMapper.findFriendTableIdByUserId(userId).getFriendId();
		return returnResultWhenTransaction(userTableMapper.deleteUserFriendTable(targetUserId, friendId), () -> findFriendEachOther(userId));

	}
	// ───────────────────────────────── helper methods ───────────────────────────────

	/**
	 * 유저 목록 반환 시 후처리 진행 한다.
	 * DTM(YYYYMMDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 * @param users 대상 유저 리스트
	 * @return 후처리 후 대상 유저 리스트
	 */
	public List<UserTableDTO> postProcessingUserTables(List<UserTableDTO> users) {

		for (UserTableDTO user : users) {
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
	public UserTableDTO postProcessingUserTable(UserTableDTO user) {

		user.setLastLoginDate(user.getLastLoginDate() != null ? commonMethod.translateDate(user.getLastLoginDate()) : null);
		user.setRegDtm(user.getRegDtm() != null ? commonMethod.translateDate(user.getRegDtm()) : null);
		user.setModDtm(user.getModDtm() != null ? commonMethod.translateDate(user.getModDtm()) : null);

		return user;
	}

	/**
	 * Transaction 결과 값을 반환 한다.
	 * @param result 결과값
	 * @return 결과값
	 */
	public <T> ResponseEntity<T> returnResultWhenTransaction(int result, Supplier<T> onSuccess)
	{
		log.info("result, {}", result);
		log.info("onSuccess.get(), {}", onSuccess.get());
		if (result == 1) return ResponseEntity.ok(onSuccess.get());
		else if (result == 0) return ResponseEntity.notFound().build();
		else return ResponseEntity.badRequest().build();
	}


}
