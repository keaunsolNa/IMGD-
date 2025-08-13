package com.nks.imgd.service.oAuth;

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
import com.nks.imgd.dto.Enum.SocialLoginType;
import com.nks.imgd.dto.userAndRole.UserTableDTO;
import com.nks.imgd.mapper.user.UserTableMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote Kakao SSO Login API
 */
@Component
@RequiredArgsConstructor
public class KakaoOauth implements SocialOauth {

	private static final Logger logger = LoggerFactory.getLogger(KakaoOauth.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private final RestTemplate restTemplate;
	private final UserTableMapper userTableMapper;
	private final com.nks.imgd.component.config.JwtTokenProvider jwtTokenProvider;
	// application.yml
	@Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
	private String KAKAO_BASE_URL;
	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	private String KAKAO_CLIENT_ID;
	@Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
	private String KAKAO_CALLBACK_URL;
	@Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
	private String KAKAO_CLIENT_SECRET;
	@Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
	private String KAKAO_TOKEN_URI;
	@Value("${spring.security.oauth2.client.registration.kakao.authorization-grant-type}")
	private String KAKAO_GRANT_TYPE;
	@Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
	private String KAKAO_USER_INFO_URI;

	/**
	 * controller 에서 요청을 받을 경우 Kakao SSO 요청을 하는 페이지 GET 방식 이동 한다.
	 *
	 * @return Request URI
	 */
	@Override
	public String getOauthRedirectURL() {

		return KAKAO_BASE_URL + "?response_type=code&client_id=" + KAKAO_CLIENT_ID + "&redirect_uri="
			+ KAKAO_CALLBACK_URL;
	}

	/**
	 * Get 요청 이후 유저가 로그인 한 후, callback page 에서 받은 verify code 통해 accessToken 요청한다.
	 *
	 * @param code : verify code
	 * @return AccessToken / RuntimeException
	 */
	@Override
	public String requestAccessToken(String code) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		Map<String, Object> params = new HashMap<>();
		params.put("grant_type", KAKAO_GRANT_TYPE);
		params.put("client_id", KAKAO_CLIENT_ID);
		params.put("code", code);
		params.put("client_secret", KAKAO_CLIENT_SECRET);

		String parameterString = params.entrySet().stream()
			.map(x -> x.getKey() + "=" + x.getValue())
			.collect(Collectors.joining("&"));

		HttpEntity<String> requestEntity = new HttpEntity<>(parameterString, headers);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(KAKAO_TOKEN_URI, requestEntity,
				String.class);

			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				return responseEntity.getBody();
			} else {
				logger.warn("Failed to retrieve Kakao token: {}", responseEntity.getStatusCode());
				throw new RuntimeException("Failed to retrieve Kakao token");
			}
		} catch (Exception e) {
			logger.warn("Exception during Kakao token retrieval: ", e);
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

		final HttpHeaders headers = new HttpHeaders();

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		JsonNode jsonNode;

		try {

			// GET 요청 전송
			ResponseEntity<String> responseEntity = restTemplate.exchange(KAKAO_USER_INFO_URI, HttpMethod.GET,
				requestEntity, String.class);

			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				jsonNode = objectMapper.readTree(responseEntity.getBody());
			} else {
				logger.warn("Failed to retrieve Kakao user info: {}", responseEntity.getStatusCode());
				throw new RuntimeException("Failed to retrieve Kakao user info");
			}

		} catch (Exception e) {
			logger.warn("Exception during Kakao user info retrieval: ", e);
			throw new RuntimeException("Exception during Kakao user info retrieval", e);
		}

		assert jsonNode != null;
		String id = jsonNode.get("id").asText();

		UserTableDTO user = userTableMapper.findById(id);

		if (null == user) {

			UserTableDTO userTableDTO = new UserTableDTO();

			userTableDTO.setUserId(id);
			userTableDTO.setName(jsonNode.get("properties").get("nickname").asText());
			userTableDTO.setEmail("익명");
			userTableDTO.setNickName(jsonNode.get("properties").get("nickname").asText());
			userTableDTO.setLoginType(SocialLoginType.KAKAO);

			userTableMapper.makeNewUser(userTableDTO);

		} else {

			user.setName(jsonNode.get("properties").get("nickname").asText());
			user.setEmail("익명");

			userTableMapper.updateUser(user);
		}

		String userRefreshToken = jwtTokenProvider.generateRefreshToken(user);
		String userAccessToken = jwtTokenProvider.generateAccessToken(user);

		assert user != null;
		logger.info("LOGIN : [{}]", user.getName());

		return new String[] {userRefreshToken, userAccessToken};

	}
}
