package com.nks.imgd.service.oAuth;

import com.nks.imgd.component.config.JwtTokenProvider;
import com.nks.imgd.dto.Enum.SocialLoginType;
import com.nks.imgd.dto.user.UserTableDTO;
import com.nks.imgd.mapper.user.UserTableMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GoogleOauth}.
 * 스타일: 기존 FileServiceTest와 동일하게 Mockito로 순수 단위 테스트를 구성.
 */
class AuthServiceTest {

	RestTemplate restTemplate;
	UserTableMapper userTableMapper;
	JwtTokenProvider jwtTokenProvider;

	GoogleOauth googleOauth;

	// 테스트용 상수 (필요한 @Value 값들)
	private static final String AUTH_URL   = "https://accounts.google.com/o/oauth2/v2/auth";
	private static final String CLIENT_ID  = "test-client-id";
	private static final String REDIRECT   = "http://localhost/oauth2/callback/google";
	private static final String SECRET     = "test-secret";
	private static final String TOKEN_URL  = "https://oauth2.googleapis.com/token";
	private static final String USERINFO   = "https://openidconnect.googleapis.com/v1/userinfo";
	private static final String GRANT_TYPE = "authorization_code";

	@BeforeEach
	void setUp() throws Exception {
		restTemplate = mock(RestTemplate.class);
		userTableMapper = mock(UserTableMapper.class);
		jwtTokenProvider = mock(JwtTokenProvider.class);

		// @RequiredArgsConstructor 로 생성되는 필드 3가지만 생성자 주입
		googleOauth = new GoogleOauth(restTemplate, userTableMapper, jwtTokenProvider);

		// 나머지 @Value 필드는 리플렉션으로 주입
		setField(googleOauth, "GOOGLE_BASE_URL", AUTH_URL);
		setField(googleOauth, "GOOGLE_CLIENT_ID", CLIENT_ID);
		setField(googleOauth, "GOOGLE_CALLBACK_URL", REDIRECT);
		setField(googleOauth, "GOOGLE_CLIENT_SECRET", SECRET);
		setField(googleOauth, "GOOGLE_TOKEN_URL", TOKEN_URL);
		setField(googleOauth, "GOOGLE_USER_INFO_URI", USERINFO);
		setField(googleOauth, "GOOGLE_GRANT_TYPE", GRANT_TYPE);
	}

	// ─────────────────────────── getOauthRedirectURL ───────────────────────────

	@Test
	@DisplayName("getOauthRedirectURL() - 필수 파라미터 포함")
	void getOauthRedirectURLContainsAllParams() {
		// ✅ When
		String url = googleOauth.getOauthRedirectURL();

		// ✅ Then
		assertTrue(url.startsWith(AUTH_URL + "?"));
		assertTrue(url.contains("client_id=" + CLIENT_ID));
		assertTrue(url.contains("redirect_uri=" + REDIRECT));
		assertTrue(url.contains("response_type=code"));
		assertTrue(url.contains("scope=")); // 상세 스코프 문자열은 인코딩/공백 이슈 방지 차원에서 포함 여부만 검증
	}

	// ─────────────────────────── requestAccessToken ───────────────────────────

	@Test
	@DisplayName("requestAccessToken() - 200 OK면 바디 반환")
	void requestAccessTokenSuccess() {
		// ✅ Given
		String code = "verify-code-123";
		ResponseEntity<String> okRes = new ResponseEntity<>("ACCESS_TOKEN_BODY", HttpStatus.OK);
		when(restTemplate.postForEntity(eq(TOKEN_URL), any(), eq(String.class))).thenReturn(okRes);

		// ✅ When
		String body = googleOauth.requestAccessToken(code);

		// ✅ Then
		assertEquals("ACCESS_TOKEN_BODY", body);
		verify(restTemplate).postForEntity(eq(TOKEN_URL), any(), eq(String.class));
	}

	@Test
	@DisplayName("requestAccessToken() - 비정상 상태코드면 RuntimeException")
	void requestAccessTokenNonOkThrows() {
		// ✅ Given
		ResponseEntity<String> bad = new ResponseEntity<>("ERR", HttpStatus.BAD_REQUEST);
		when(restTemplate.postForEntity(eq(TOKEN_URL), any(), eq(String.class))).thenReturn(bad);

		// ✅ Then
		assertThrows(RuntimeException.class, () -> googleOauth.requestAccessToken("bad-code"));
	}

	// ─────────────────────────── requestUserInfo ───────────────────────────

	@Test
	@DisplayName("requestUserInfo() - 기존 사용자면 업데이트 후 토큰 2개 반환")
	void requestUserInfo_existingUserSuccess() {
		// ✅ Given
		String accessToken = "at-123";
		String userJson = "{\"sub\":\"ksna\",\"name\":\"Alice\",\"email\":\"knsol19921@gmail.com\"}";

		// Google userinfo 응답
		when(restTemplate.exchange(eq(USERINFO), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
			.thenReturn(new ResponseEntity<>(userJson, HttpStatus.OK));

		// 기존 사용자 존재
		UserTableDTO existing = new UserTableDTO();
		existing.setUserId("ksna");
		existing.setName("나큰솔");
		existing.setEmail("knsol19921@gmail.com");
		existing.setLoginType(SocialLoginType.GOOGLE);

		when(userTableMapper.findById("ksna")).thenReturn(existing);

		// 현재 구현은 else 분기에서 update가 아닌 makeNewUser(user)를 호출하므로 그에 맞게 검증
		when(jwtTokenProvider.generateRefreshToken(existing)).thenReturn("REFRESH");
		when(jwtTokenProvider.generateAccessToken(existing)).thenReturn("ACCESS");

		// ✅ When
		String[] tokens = googleOauth.requestUserInfo(accessToken);

		// ✅ Then
		assertArrayEquals(new String[]{"REFRESH", "ACCESS"}, tokens);

		InOrder io = inOrder(restTemplate, userTableMapper, jwtTokenProvider);
		io.verify(restTemplate).exchange(eq(USERINFO), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
		io.verify(userTableMapper).findById("ksna");

		// updateUser가 호출되고, 값이 JSON 값으로 갱신되었는지 검증
		io.verify(userTableMapper).updateUser(argThat(u ->
			"ksna".equals(u.getUserId()) &&
				"Alice".equals(u.getName()) &&
				"knsol19921@gmail.com".equals(u.getEmail())
		));

		io.verify(jwtTokenProvider).generateRefreshToken(existing);
		io.verify(jwtTokenProvider).generateAccessToken(existing);
	}

	@Test
	@DisplayName("requestUserInfo() - 신규 사용자 분기, 생성 후 토큰 2개 반환")
	void requestUserInfoNewUserCurrentBugThrowsNpe() {
		String accessToken = "at-456";
		String userJson = "{\"sub\":\"g789\",\"name\":\"Bob\",\"email\":\"bob@example.com\"}";

		when(restTemplate.exchange(eq(USERINFO), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
			.thenReturn(new ResponseEntity<>(userJson, HttpStatus.OK));

		// 조회 결과 없음 → 신규 경로
		when(userTableMapper.findById("g789")).thenReturn(null);

		// 토큰은 신규 user DTO로 발급
		when(jwtTokenProvider.generateRefreshToken(any(UserTableDTO.class))).thenReturn("REFRESH");
		when(jwtTokenProvider.generateAccessToken(any(UserTableDTO.class))).thenReturn("ACCESS");

		String[] tokens = googleOauth.requestUserInfo(accessToken);

		assertArrayEquals(new String[]{"REFRESH", "ACCESS"}, tokens);

		InOrder io = inOrder(restTemplate, userTableMapper, jwtTokenProvider);
		io.verify(restTemplate).exchange(eq(USERINFO), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
		io.verify(userTableMapper).findById("g789");

		// 랜덤 닉네임이라 전체 동등 비교 대신 핵심 필드만 검증
		io.verify(userTableMapper).makeNewUser(argThat(u ->
			u != null &&
				"g789".equals(u.getUserId()) &&
				"Bob".equals(u.getName()) &&
				"bob@example.com".equals(u.getEmail()) &&
				u.getLoginType() == SocialLoginType.GOOGLE
		));

		io.verify(jwtTokenProvider).generateRefreshToken(any(UserTableDTO.class));
		io.verify(jwtTokenProvider).generateAccessToken(any(UserTableDTO.class));
	}

	// ─────────────────────────── helpers ───────────────────────────

	private static void setField(Object target, String name, Object value) throws Exception {
		Field f = target.getClass().getDeclaredField(name);
		f.setAccessible(true);
		f.set(target, value);
	}
}
