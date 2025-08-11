package com.nks.imgd.service.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nks.imgd.controller.group.GroupController;
import com.nks.imgd.dto.GroupTableDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(GroupController.class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@WithMockUser("nks")
public class GroupControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GroupService groupService;

	@Autowired
	private ObjectMapper objectMapper;

	private GroupTableDTO savedGroup; // 🔹 필드로 선언

	@BeforeEach
	void setUp() {
		savedGroup = new GroupTableDTO();
		savedGroup.setGroupNm("테스트 그룹");
		savedGroup.setGroupMstUserId("ksna");
	}

	@Test
	@DisplayName("POST /group - 그룹 생성 API 테스트")
	void makeNewGroupTest() throws Exception {

		// ✅ given
		// BeforeEach 로 대체 한다.

		// ✅ when & then
		when(groupService.makeNewGroup(savedGroup)).thenReturn(1);
		mockMvc.perform(post("/group/makeGroup")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(savedGroup)))
				.andExpect(status().isOk())
				.andExpect(content().string("그룹 생성 완료"))
				.andDo(document("group-create",
						requestFields(
								fieldWithPath("groupId").optional().type(JsonFieldType.NUMBER).description("그룹 ID (자동 생성)"),
								fieldWithPath("groupNm").type(JsonFieldType.STRING).description("그룹 이름"),
								fieldWithPath("groupMstUserId").type(JsonFieldType.STRING).description("그룹 마스터 사용자 ID"),
								fieldWithPath("regDtm").optional().type(JsonFieldType.STRING).description("등록 일시"),
								fieldWithPath("regId").optional().type(JsonFieldType.STRING).description("등록자 ID"),
								fieldWithPath("modDtm").optional().type(JsonFieldType.STRING).description("수정 일시"),
								fieldWithPath("modId").optional().type(JsonFieldType.STRING).description("수정자 ID")
						),
						responseBody()
				));
	}

	@Test
	@DisplayName("POST /group - 그룹 유저 Row 추가 테스트")
	void makeNewGroupUserTest() throws Exception {

		// ✅ given
		// BeforeEach 로 대체 한다.

		// ✅ when & then
		when(groupService.makeNewGroupUser(any(GroupTableDTO.class), eq("test"))).thenReturn(1);

		mockMvc.perform(post("/group/addGroupUser")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.param("userId", "test")
						.content(objectMapper.writeValueAsString(savedGroup)))
				.andExpect(status().isOk())
				.andExpect(content().string("그룹 유저 생성 완료"))
				.andDo(document("groupUser-create",
						requestFields(
								fieldWithPath("groupId").optional().type(JsonFieldType.NUMBER).description("그룹 ID (자동 생성)"),
								fieldWithPath("groupNm").type(JsonFieldType.STRING).description("그룹 이름"),
								fieldWithPath("groupMstUserId").type(JsonFieldType.STRING).description("그룹 마스터 사용자 ID"),
								fieldWithPath("regDtm").optional().type(JsonFieldType.STRING).description("등록 일시"),
								fieldWithPath("regId").optional().type(JsonFieldType.STRING).description("등록자 ID"),
								fieldWithPath("modDtm").optional().type(JsonFieldType.STRING).description("수정 일시"),
								fieldWithPath("modId").optional().type(JsonFieldType.STRING).description("수정자 ID")
						),
						responseBody()
				));
	}

	@Test
	@DisplayName("DELETE / group - 그룹 유저 Row 삭제 테스트")
	void deleteGroupUser() throws Exception {

		// ✅ given
		savedGroup.setGroupId(1L);

		// ✅ when & then
		when(groupService.deleteGroupUser(any(GroupTableDTO.class), eq("test"))).thenReturn(1);

		// 정상 적인 삭제 과정
		mockMvc.perform(delete("/group/deleteGroupUser")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.param("userId", "test")
				.content(objectMapper.writeValueAsString(savedGroup)))
			.andExpect(status().isOk())
			.andExpect(content().string("Complete delete group user."))
			.andDo(document("groupUserDelete",
					requestFields(
							fieldWithPath("groupId").optional().type(JsonFieldType.NUMBER).description("그룹 ID (자동 생성)"),
							fieldWithPath("groupNm").type(JsonFieldType.STRING).description("그룹 이름"),
							fieldWithPath("groupMstUserId").type(JsonFieldType.STRING).description("그룹 마스터 사용자 ID"),
							fieldWithPath("regDtm").optional().type(JsonFieldType.STRING).description("등록 일시"),
							fieldWithPath("regId").optional().type(JsonFieldType.STRING).description("등록자 ID"),
							fieldWithPath("modDtm").optional().type(JsonFieldType.STRING).description("수정 일시"),
							fieldWithPath("modId").optional().type(JsonFieldType.STRING).description("수정자 ID")
					),
					responseBody()
			));

		when(groupService.deleteGroupUser(any(GroupTableDTO.class), eq("ksna"))).thenReturn(1);
		// 삭제 하려는 계정이 MST 계정일 경우
		mockMvc.perform(delete("/group/deleteGroupUser")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.param("userId", "ksna")
						.content(objectMapper.writeValueAsString(savedGroup)))
				.andExpect(status().isOk())
				.andExpect(content().string("Complete delete group user."))
				.andDo(document("groupUserDelete",
						requestFields(
								fieldWithPath("groupId").optional().type(JsonFieldType.NUMBER).description("그룹 ID (자동 생성)"),
								fieldWithPath("groupNm").type(JsonFieldType.STRING).description("그룹 이름"),
								fieldWithPath("groupMstUserId").type(JsonFieldType.STRING).description("그룹 마스터 사용자 ID"),
								fieldWithPath("regDtm").optional().type(JsonFieldType.STRING).description("등록 일시"),
								fieldWithPath("regId").optional().type(JsonFieldType.STRING).description("등록자 ID"),
								fieldWithPath("modDtm").optional().type(JsonFieldType.STRING).description("수정 일시"),
								fieldWithPath("modId").optional().type(JsonFieldType.STRING).description("수정자 ID")
						),
						responseBody()
				));
	}
}