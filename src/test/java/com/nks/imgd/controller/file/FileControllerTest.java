package com.nks.imgd.controller.file;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nks.imgd.dto.GroupTableDTO;
import com.nks.imgd.service.file.FileService;

@WebMvcTest(FileController.class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets/file")
@WithMockUser("nks")
public class FileControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private FileService fileService;

	@Autowired
	private ObjectMapper objectMapper;

	private GroupTableDTO savedGroup; //

	@BeforeEach
	void setUp() {
		savedGroup = new GroupTableDTO();
		savedGroup.setGroupId(1L);
		savedGroup.setGroupNm("테스트 그룹");
		savedGroup.setGroupMstUserId("ksna");
	}

	@Test
	@DisplayName("POST /file - 그룹 루트 디렉터리 API 생성 테스트")
	void makeGroupDir() throws Exception {

		// ✅ given
		// BeforeEach 로 대체 한다.

		// ✅ when & then
		when(fileService.makeGroupDir(savedGroup)).thenReturn(1);
		mockMvc.perform(post("/file/makeGroupDir")
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(savedGroup)))
		.andExpect(status().isOk())
		.andExpect(content().string("Complete make group root directory"))
		.andDo(document("file-create",
			requestFields(
				fieldWithPath("groupId").type(JsonFieldType.NUMBER).description("그룹 ID"),
				fieldWithPath("groupNm").type(JsonFieldType.STRING).description("그룹명"),
				fieldWithPath("groupMstUserId").type(JsonFieldType.STRING).description("그룹 마스터 사용자 ID"),
				fieldWithPath("regDtm").optional().type(JsonFieldType.STRING).description("등록 일시"),
				fieldWithPath("regId").optional().type(JsonFieldType.STRING).description("등록자"),
				fieldWithPath("modDtm").optional().type(JsonFieldType.STRING).description("수정 일시"),
				fieldWithPath("modId").optional().type(JsonFieldType.STRING).description("수정자")
			),
			responseBody()
		));

	}
}
