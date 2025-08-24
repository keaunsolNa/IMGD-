package com.nks.imgd.service.user;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.dto.userAndRole.UserTableDTO;
import com.nks.imgd.mapper.user.UserTableMapper;
import com.nks.imgd.service.file.FileService;

@Service
public class UserService {

	private final UserTableMapper userTableMapper;
	private final CommonMethod commonMethod = new CommonMethod();
	private final FileService fileService;

	public UserService(UserTableMapper userTableMapper, FileService fileService)
	{
		this.userTableMapper = userTableMapper;
		this.fileService = fileService;
	}

	/**
	 * 로그인 한 유저가 가지고 있는 회원 정보를 반환한다.
	 *
	 * @param userId 대상 유저 ID
	 * @return 대상 유저가 가지고 있는 정보
	 */
	public UserTableDTO findUserById(@Param("userId") String userId) {

		UserTableDTO userTableDTO = userTableMapper.findById(userId);
		userTableDTO.setLastLoginDate(commonMethod.translateDate(userTableDTO.getLastLoginDate()));
		userTableDTO.setRegDtm(commonMethod.translateDate(userTableDTO.getRegDtm()));

		Long fileId = userTableDTO.getPictureId();

		if (null != fileId)
		{
			String chainUrl = fileService.selectRootPath(fileId); // "/GROUP_IMG/1_테스트그룹1"
			userTableDTO.setPictureUrl(chainUrl);
		}

		return userTableDTO;
	}
	
	/**
	 * 로그인 한 유저가 가지고 있는 회원 정보를 변경한다.
	 *
	 * @param user 대상 유저 DTO
	 * @return 대상 유저가 가지고 있는 정보
	 */
	@Transactional(rollbackFor = Exception.class)
	public int updateUser(@Param("user") UserTableDTO user) {

		int result = userTableMapper.updateUser(user);

		if (result == 1)
		{
			return 1;
		}
		else if (result == 0) return 0;

		else {
			throw new IllegalStateException("Multi Row Updated");
		}
	}
}
