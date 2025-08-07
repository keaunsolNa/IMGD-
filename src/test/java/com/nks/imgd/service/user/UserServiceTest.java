package com.nks.imgd.service.user;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nks.imgd.dto.UserTableDTO;

@SpringBootTest
public class UserServiceTest {

	private static final Logger log = LoggerFactory.getLogger(UserServiceTest.class);
	@Autowired
	private UserService userService;

	@Test
	void testFindAllUser() {

		log.info("✅ UserService.findAllUser() 실행");

		List<UserTableDTO> userList = userService.findAllUsers();

		log.info("✅ 조회된 데이터 개수 : " + userList.size());

		System.out.println("한글");
		for (UserTableDTO userTableDTO : userList) {

			log.info(userTableDTO.toString());
		}


	}
}
