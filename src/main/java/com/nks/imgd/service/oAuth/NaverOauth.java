package com.nks.imgd.service.oAuth;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nks.imgd.component.config.JwtTokenProvider;
import com.nks.imgd.component.util.maker.RandomNickNameMaker;
import com.nks.imgd.dto.Enum.SocialLoginType;
import com.nks.imgd.dto.userAndRole.UserTableDTO;
import com.nks.imgd.mapper.user.UserTableMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote Naver SSO Login API
 */
@Component
@RequiredArgsConstructor
public class NaverOauth implements SocialOauth {

	private static final Logger logger = LoggerFactory.getLogger(NaverOauth.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private final RestTemplate restTemplate;
	private final UserTableMapper userTableMapper;
	private final JwtTokenProvider jwtTokenProvider;
	// application.yml
	@Value("${spring.security.oauth2.client.provider.naver.authorization-uri}")
	private String NAVER_BASE_URL;
	@Value("${spring.security.oauth2.client.registration.naver.client-id}")
	private String NAVER_CLIENT_ID;
	@Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
	private String NAVER_CALLBACK_URL;
	@Value("${spring.security.oauth2.client.registration.naver.client-secret}")
	private String NAVER_CLIENT_SECRET;
	@Value("${spring.security.oauth2.client.provider.naver.token-uri}")
	private String NAVER_TOKEN_URI;
	@Value("${spring.security.oauth2.client.registration.naver.authorization-grant-type}")
	private String NAVER_GRANT_TYPE;
	@Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
	private String NAVER_USER_INFO_URI;

	/**
	 * controller 에서 요청을 받을 경우 Naver SSO 요청을 하는 페이지 GET 방식 이동 한다.
	 *
	 * @return Request URI
	 */
	@Override
	public String getOauthRedirectURL() {

		Map<String, Object> params = new HashMap<>();
		params.put("response_type", "code");
		params.put("client_id", NAVER_CLIENT_ID);
		params.put("redirect_uri", NAVER_CALLBACK_URL);
		SecureRandom random = new SecureRandom();
		String state = new BigInteger(130, random).toString();
		params.put("state", state);
		params.put("grant_type", NAVER_GRANT_TYPE);

		String parameterString = params.entrySet().stream()
			.map(x -> x.getKey() + "=" + x.getValue())
			.collect(Collectors.joining("&"));

		return NAVER_BASE_URL + "?" + parameterString;
	}

	/**
	 * Get 요청 이후 유저가 로그인 한 후, callback page 에서 받은 verify code 통해 accessToken 요청한다.
	 *
	 * @param code : verify code
	 * @return AccessToken / RuntimeException
	 */
	@Override
	public String requestAccessToken(String code) {

		String apiURL = NAVER_TOKEN_URI +
			"?grant_type=" + NAVER_GRANT_TYPE +
			"&client_id=" + NAVER_CLIENT_ID +
			"&client_secret=" + NAVER_CLIENT_SECRET +
			"&redirect_uri=" + NAVER_CALLBACK_URL +
			"&code=" + code;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		try {
			ResponseEntity<String> responseEntity = restTemplate.exchange(
				apiURL,
				HttpMethod.GET,
				requestEntity,
				String.class
			);

			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				return responseEntity.getBody();
			} else {
				logger.warn("Failed to retrieve access token: {}", responseEntity.getBody());
				throw new RuntimeException("Failed to retrieve naver token");
			}

		} catch (Exception e) {
			logger.warn("Error during access token request: {}", e.getMessage());
			throw new RuntimeException("Exception during Kakao token retrieval", e);
		}

	}

	/**
	 * AccessToken 받은 후 user 정보를 요청하는 API
	 * userInfo 를 받은 경우, 해당하는 id가 sso-user-index 에 있다면 update, 없다면 insert 수행
	 *
	 * @param accessToken : 전달받은 AccessToken
	 * @return 반환될 JWT Token
	 */
	@Override
	public String[] requestUserInfo(String accessToken) {

		HttpHeaders headers = new HttpHeaders();

		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

		final HttpEntity<String> httpEntity = new HttpEntity<>(headers);

		String userInfo = restTemplate.exchange(NAVER_USER_INFO_URI, HttpMethod.GET, httpEntity, String.class)
			.getBody();

		JsonNode jsonNode;

		try {

			// GET 요청 전송
			ResponseEntity<String> responseEntity = restTemplate.exchange(NAVER_USER_INFO_URI, HttpMethod.GET,
				httpEntity, String.class);

			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				jsonNode = objectMapper.readTree(userInfo).get("response");
			} else {
				logger.warn("Failed to retrieve Naver user info: {}", responseEntity.getStatusCode());
				throw new RuntimeException("Failed to retrieve Naver user info");
			}

		} catch (Exception e) {
			logger.warn("Exception during Naver user info retrieval: ", e);
			throw new RuntimeException("Exception during Naver user info retrieval", e);
		}

		assert jsonNode != null;
		String id = jsonNode.get("id").asText();

		UserTableDTO user = userTableMapper.findById(id);

		if (null == user) {

			RandomNickNameMaker randomNickNameMaker = new RandomNickNameMaker();
			UserTableDTO userTableDTO = new UserTableDTO();

			userTableDTO.setUserId(id);
			userTableDTO.setName(jsonNode.get("name").asText());
			userTableDTO.setEmail(jsonNode.get("email").asText());
			userTableDTO.setNickName(randomNickNameMaker.makeRandomNickName());
			userTableDTO.setLoginType(SocialLoginType.NAVER);

			userTableMapper.makeNewUser(userTableDTO);

		} else {

			user.setName(jsonNode.get("name").asText());
			user.setEmail(jsonNode.get("email").asText());

			userTableMapper.updateUser(user);
		}

		String userRefreshToken = jwtTokenProvider.generateRefreshToken(user);
		String userAccessToken = jwtTokenProvider.generateAccessToken(user);

		assert user != null;
		logger.info("LOGIN : [{}]", user.getName());

		return new String[] {userRefreshToken, userAccessToken};
	}
}
