// package com.nks.imgd.controller.oAuth;
//
// import com.nks.imgd.component.config.JwtTokenProvider;
// import com.nks.imgd.component.util.maker.TokenMaker;
// import com.nks.imgd.dto.Enum.SocialLoginType;
// import com.nks.imgd.dto.user.UserTableDTO;
// import com.nks.imgd.service.oAuth.AuthService;
// import jakarta.servlet.http.Cookie;
// import jakarta.servlet.http.HttpServletResponse;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.mockito.InOrder;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.http.MediaType;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.test.web.servlet.MockMvc;
//
// import static org.mockito.Mockito.*;           // verify, inOrder 등용
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.argThat;
//
// import static org.hamcrest.Matchers.startsWith;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//
// @WebMvcTest(controllers = OauthController.class)
// @AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 비활성화
// class AuthControllerTest {
//
// 	@Autowired
// 	MockMvc mockMvc;
//
// 	@MockitoBean
// 	AuthService authService;
//
// 	@MockitoBean
// 	JwtTokenProvider jwtTokenProvider;
//
// 	@MockitoBean
// 	TokenMaker tokenMaker;
//
// 	// ───────────── /auth/{socialLoginType} ─────────────
//
// 	@Test
// 	@DisplayName("GET /auth/GOOGLE → authService.request 호출 및 200 반환")
// 	void socialLoginType_ok() throws Exception {
// 		when(authService.request(SocialLoginType.GOOGLE))
// 			.thenReturn(new java.util.HashMap<>() {{
// 				put("authUrl", "https://accounts.google.com/o/oauth2/v2/auth?...");
// 			}});
//
// 		mockMvc.perform(get("/auth/GOOGLE"))
// 			.andExpect(status().isOk())
// 			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
// 			// ✨ 체이닝 형태로 Hamcrest 매처 사용
// 			.andExpect(jsonPath("$.authUrl").value(startsWith("https://accounts.google.com/")));
//
// 		verify(authService).request(SocialLoginType.GOOGLE);
// 	}
//
// 	// ───────────── /auth/login/{socialLoginType}/callback (GOOGLE) ─────────────
//
// 	@Test
// 	@DisplayName("POST /auth/login/GOOGLE/callback → requestUserInfo 경로, 쿠키/응답 검증")
// 	void callback_google_ok() throws Exception {
// 		when(authService.requestUserInfo("abc123"))
// 			.thenReturn(new String[]{"REFRESH_TOKEN", "ACCESS_TOKEN"});
//
// 		mockMvc.perform(post("/auth/login/GOOGLE/callback")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content("{\"authorizationCode\":\"abc123\"}"))
// 			.andExpect(status().isOk())
// 			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
// 			// 본 컨트롤러는 accessToken을 Cookie 객체로 바디에 넣어 반환함
// 			.andExpect(jsonPath("$.redirectUrl").value("/"))
// 			.andExpect(jsonPath("$.accessToken.name").value("accessToken"))
// 			.andExpect(jsonPath("$.accessToken.value").value("ACCESS_TOKEN"));
//
// 		// 호출 순서 및 파라미터 검증
// 		InOrder io = inOrder(authService, tokenMaker);
// 		io.verify(authService).requestUserInfo("abc123");
//
// 		// refreshToken은 HttpServletResponse와 함께 전달됨
// 		io.verify(tokenMaker).makeRefreshToken(any(HttpServletResponse.class),
// 			argThat(c -> c != null && "refreshTokenForKnock".equals(c.getName())
// 				&& "REFRESH_TOKEN".equals(c.getValue())));
//
// 		// accessToken은 Cookie만 전달됨
// 		io.verify(tokenMaker).makeAccessToken(argThat(c ->
// 			c != null && "accessToken".equals(c.getName())
// 				&& "ACCESS_TOKEN".equals(c.getValue())));
// 	}
//
// 	// ───────────── /auth/login/{socialLoginType}/callback (NAVER 등 비-GOOGLE) ─────────────
//
// 	@Test
// 	@DisplayName("POST /auth/login/NAVER/callback → requestAccessToken 경로, 쿠키/응답 검증")
// 	void callback_naver_ok() throws Exception {
// 		when(authService.requestAccessToken(SocialLoginType.NAVER, "code-777"))
// 			.thenReturn(new String[]{"REFRESH_N", "ACCESS_N"});
//
// 		mockMvc.perform(post("/auth/login/NAVER/callback")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content("{\"authorizationCode\":\"code-777\"}"))
// 			.andExpect(status().isOk())
// 			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
// 			.andExpect(jsonPath("$.redirectUrl").value("/"))
// 			.andExpect(jsonPath("$.accessToken.name").value("accessToken"))
// 			.andExpect(jsonPath("$.accessToken.value").value("ACCESS_N"));
//
// 		InOrder io = inOrder(authService, tokenMaker);
// 		io.verify(authService).requestAccessToken(SocialLoginType.NAVER, "code-777");
// 		io.verify(tokenMaker).makeRefreshToken(any(HttpServletResponse.class),
// 			argThat(c -> c != null && "refreshTokenForKnock".equals(c.getName())
// 				&& "REFRESH_N".equals(c.getValue())));
// 		io.verify(tokenMaker).makeAccessToken(argThat(c ->
// 			c != null && "accessToken".equals(c.getName())
// 				&& "ACCESS_N".equals(c.getValue())));
// 		verify(authService, never()).requestUserInfo(anyString()); // 비-GOOGLE 경로에서는 호출되지 않음
// 	}
//
// 	// ───────────── /auth/getAccessToken ─────────────
//
// 	@Test
// 	@DisplayName("POST /auth/getAccessToken → refresh에서 access 발급, Cookie 및 JSON 응답 검증")
// 	void getAccessToken_ok() throws Exception {
// 		when(jwtTokenProvider.resolveToken(any())).thenReturn("REFRESH_ABC");
//
// 		UserTableDTO user = new UserTableDTO();
// 		user.setUserId("u1");
// 		when(jwtTokenProvider.getUserDetails("REFRESH_ABC")).thenReturn(user);
// 		when(jwtTokenProvider.generateAccessToken(user)).thenReturn("ACCESS_NEW");
//
// 		mockMvc.perform(post("/auth/getAccessToken"))
// 			.andExpect(status().isOk())
// 			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
// 			.andExpect(jsonPath("$.redirectUrl").value("/"))
// 			.andExpect(jsonPath("$.accessToken").value("ACCESS_NEW"));
//
// 		// accessToken Cookie가 만들어지는지 검증 (TokenMaker에 전달됨)
// 		verify(tokenMaker).makeAccessToken(argThat((Cookie c) ->
// 			c != null && "accessToken".equals(c.getName())
// 				&& "ACCESS_NEW".equals(c.getValue())));
// 	}
//
// 	// ───────────── /auth/logout ─────────────
//
// 	@Test
// 	@DisplayName("POST /auth/logout → refreshTokenForKnock 무효화 시도 후 200")
// 	void logout_ok() throws Exception {
// 		mockMvc.perform(post("/auth/logout")
// 				.cookie(new Cookie("refreshTokenForKnock", "REFRESH_XYZ")))
// 			.andExpect(status().isOk());
//
// 		// 무효화 호출 검증
// 		verify(tokenMaker).makeTokenValidOut(argThat(c ->
// 			c != null && "refreshTokenForKnock".equals(c.getName())));
// 	}
// }
