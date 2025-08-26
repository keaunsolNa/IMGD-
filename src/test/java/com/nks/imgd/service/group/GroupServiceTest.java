// package com.nks.imgd.service.group;
//
// import static org.mockito.Mockito.*;
//
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
//
// import com.nks.imgd.dto.group.GroupTableDTO;
// import com.nks.imgd.mapper.group.GroupTableMapper;
//
// /**
//  * Unit test for {@link GroupService}.
//  * <p>
//  * This test verifies that a group is correctly inserted through
//  * {@link GroupService#makeNewGroup(GroupTableDTO)}
//  * using a mocked {@link GroupTableMapper}.
//  */
// public class GroupServiceTest {
//
// 	@Test
// 	@DisplayName("그룹 생성 makeGroup 테스트")
// 	void testMakeGroup() {
//
// 		// ✅ Arrange
// 		GroupTableDTO groupDto = new GroupTableDTO();
// 		groupDto.setGroupNm("테스트 그룹");
// 		groupDto.setGroupMstUserId("ksna");
//
// 		GroupTableMapper mockMapper = mock(GroupTableMapper.class);
// 		when(mockMapper.makeNewGroup(groupDto)).thenReturn(1); // 1 row inserted
//
// 		GroupService groupService = new GroupService(mockMapper);
//
// 		// ✅ Act
// //		int result = groupService.makeNewGroup(groupDto);
//
// 		// ✅ Assert
// //		assertEquals(1, result, "Complete make group.");
// 		verify(mockMapper, times(1)).makeNewGroup(groupDto); // 호출 여부 확인
// 	}
// }
