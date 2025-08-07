package com.nks.imgd.service.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nks.imgd.controller.group.GroupController;
import com.nks.imgd.dto.GroupTableDTO;
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

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

	@Test
	@DisplayName("POST /group - 그룹 생성 API 테스트")
	void createGroupTest() throws Exception {
		// ✅ given
		GroupTableDTO dto = new GroupTableDTO();
		dto.setGroupNm("테스트 그룹");
		dto.setGroupMstUserId("ksna");

		when(groupService.makeGroup(dto)).thenReturn(1);

		// ✅ when & then
		mockMvc.perform(post("/group")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
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
}