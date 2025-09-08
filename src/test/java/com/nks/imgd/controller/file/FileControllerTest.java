// package com.nks.imgd.controller.file;
//
// import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
// import static org.springframework.restdocs.payload.PayloadDocumentation.*;
// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.http.MediaType;
// import org.springframework.restdocs.payload.JsonFieldType;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.test.web.servlet.MockMvc;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.nks.imgd.dto.data.MakeDirDTO;
// import com.nks.imgd.dto.dataDTO.GroupTableDTO;
// import com.nks.imgd.service.file.FileService;
//
// @WebMvcTest(FileController.class)
// @AutoConfigureRestDocs(outputDir = "build/generated-snippets/file/controller")
// @WithMockUser("nks")
// public class FileControllerTest {
//
// 	@Autowired
// 	private MockMvc mockMvc;
//
// 	@MockitoBean
// 	private FileService fileService;
//
// 	@Autowired
// 	private ObjectMapper objectMapper;
//
// 	@Test
// 	@DisplayName("POST /file/makeGroupDir - 성공")
// 	void makeGroupDirSuccess() throws Exception {
//
// 		// ✅ Given
// 		GroupTableDTO dto = new GroupTableDTO();
// 		dto.setGroupId(1L);
// 		dto.setGroupNm("테스트 그룹");
// 		dto.setGroupMstUserId("ksna");
//
// 		// ✅ When
// 		// when(fileService.makeGroupDir(any())).thenReturn(1);
//
// 		// ✅ Then
// 		mockMvc.perform(post("/file/makeGroupDir")
// 				.with(csrf())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(dto)))
// 			.andExpect(status().isOk())
// 			.andExpect(content().string("Complete make group root directory"))
// 			.andDo(document("file-makeGroupDir",
// 				requestFields(
// 					fieldWithPath("groupId").type(JsonFieldType.NUMBER).description("그룹 ID"),
// 					fieldWithPath("groupNm").type(JsonFieldType.STRING).description("그룹명"),
// 					fieldWithPath("groupMstUserId").type(JsonFieldType.STRING).description("그룹 마스터 사용자 ID"),
// 					fieldWithPath("regDtm").optional().type(JsonFieldType.STRING).description("등록 일시"),
// 					fieldWithPath("regId").optional().type(JsonFieldType.STRING).description("등록자"),
// 					fieldWithPath("modDtm").optional().type(JsonFieldType.STRING).description("수정 일시"),
// 					fieldWithPath("modId").optional().type(JsonFieldType.STRING).description("수정자")
// 				),
// 				responseBody()
// 			));
// 	}
//
// 	@Test
// 	@DisplayName("POST /file/makeGroupDir - 실패")
// 	void makeGroupDirFail() throws Exception {
//
// 		// ✅ Given
// 		GroupTableDTO dto = new GroupTableDTO();
// 		dto.setGroupId(2L);
// 		dto.setGroupNm("그룹2");
// 		dto.setGroupMstUserId("ksnaIsNot");
//
// 		// ✅ When
// 		// when(fileService.makeGroupDir(any())).thenReturn(-1);
//
// 		// ✅ Then
// 		mockMvc.perform(post("/file/makeGroupDir")
// 				.with(csrf())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(dto)))
// 			.andExpect(status().is5xxServerError())
// 			.andExpect(content().string("Failed make group root directory"));
// 	}
//
// 	@Test
// 	@DisplayName("POST /file/makeDir - 성공")
// 	void makeDirSuccess() throws Exception {
//
// 		// ✅ Given
// 		MakeDirDTO req = new MakeDirDTO();
// 		req.setUserId("ksna");
// 		req.setParentId(3L);
// 		req.setGroupId(1L);
// 		req.setDirNm("추억");
//
// 		// ✅ When
// 		// when(fileService.makeDir(eq(req))).thenReturn(1);
//
// 		// ✅ Then
// 		mockMvc.perform(post("/file/makeDir")
// 				.with(csrf())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(req)))
// 			.andExpect(status().isOk())
// 			.andExpect(content().string("Complete make group root directory"));
// 	}
//
// 	@Test
// 	@DisplayName("POST /file/makeDir - 실패")
// 	void makeDirFail() throws Exception {
//
// 		// ✅ Given
// 		MakeDirDTO req = new MakeDirDTO();
// 		req.setUserId("ksnaIsNot");
// 		req.setParentId(10L);
// 		req.setGroupId(1L);
// 		req.setDirNm("추억");
//
// 		// ✅ When
// 		// when(fileService.makeDir(req)).thenReturn(-1);
//
// 		// ✅ Then
// 		mockMvc.perform(post("/file/makeDir")
// 				.with(csrf())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(req)))
// 			.andExpect(status().is5xxServerError())
// 			.andExpect(content().string("Failed make group root directory"));
// 	}
// }
