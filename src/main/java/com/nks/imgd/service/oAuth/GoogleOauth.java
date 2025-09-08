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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nks.imgd.component.config.JwtTokenProvider;
import com.nks.imgd.component.util.maker.RandomNickNameMaker;
import com.nks.imgd.dto.Enum.SocialLoginType;
import com.nks.imgd.dto.dataDTO.UserTableWithRelationshipAndPictureNmDTO;
import com.nks.imgd.mapper.user.UserTableMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote Google SSO Login API
 */
@Component
@RequiredArgsConstructor
public class GoogleOauth implements SocialOauth {

	private static final Logger logger = LoggerFactory.getLogger(GoogleOauth.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private final RestTemplate restTemplate;
	private final UserTableMapper userTableMapper;
	private final JwtTokenProvider jwtTokenProvider;
	// application.yml
	@Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
	private String GOOGLE_BASE_URL;
	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String GOOGLE_CLIENT_ID;
	@Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
	private String GOOGLE_CALLBACK_URL;
	@Value("${spring.security.oauth2.client.registration.google.client-secret}")
	private String GOOGLE_CLIENT_SECRET;
	@Value("${spring.security.oauth2.client.provider.google.token-uri}")
	private String GOOGLE_TOKEN_URL;
	@Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
	private String GOOGLE_USER_INFO_URI;
	@Value("${spring.security.oauth2.client.registration.google.authorization-grant-type}")
	private String GOOGLE_GRANT_TYPE;

	/**
	 * controller 에서 요청을 받을 경우 Google SSO 요청을 하는 페이지 GET 방식 이동 한다.
	 *
	 * @return Request URI
	 */
	@Override
	public String getOauthRedirectURL() {

		Map<String, Object> params = new HashMap<>();
		params.put("scope",
			"openid https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email");
		params.put("response_type", "code");
		params.put("client_id", GOOGLE_CLIENT_ID);
		params.put("redirect_uri", GOOGLE_CALLBACK_URL);

		String parameterString = params.entrySet().stream()
			.map(x -> x.getKey() + "=" + x.getValue())
			.collect(Collectors.joining("&"));

		return GOOGLE_BASE_URL + "?" + parameterString;
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

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("code", code);
		form.add("client_id", GOOGLE_CLIENT_ID);
		form.add("client_secret", GOOGLE_CLIENT_SECRET);
		form.add("redirect_uri", GOOGLE_CALLBACK_URL); // authorize 때와 1글자도 동일해야 함
		form.add("grant_type", GOOGLE_GRANT_TYPE);     // authorization_code
		//
		// Map<String, Object> params = new HashMap<>();
		// params.put("code", code);
		// params.put("client_id", GOOGLE_CLIENT_ID);
		// params.put("client_secret", GOOGLE_CLIENT_SECRET);
		// params.put("redirect_uri", GOOGLE_CALLBACK_URL);
		// params.put("grant_type", GOOGLE_GRANT_TYPE);

		try {

			HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);
			ResponseEntity<String> res =
				restTemplate.postForEntity(GOOGLE_TOKEN_URL, req, String.class);

			// ResponseEntity<String> responseEntity = restTemplate.postForEntity(GOOGLE_TOKEN_URL, params, String.class);

			// if (responseEntity.getStatusCode() == HttpStatus.OK) {
			// 	return responseEntity.getBody();
			// } else {
			// 	logger.warn("Failed to retrieve Google token: {}", responseEntity.getStatusCode());
			// 	throw new RuntimeException("Failed to retrieve Google token");
			// }

			if (res.getStatusCode() == HttpStatus.OK) {
				return res.getBody();
			} else {
				logger.warn("Failed to retrieve Google token: {}", res.getStatusCode());
				throw new RuntimeException("Failed to retrieve Google token");
			}

		} catch (Exception e) {
			logger.warn("Exception during Google token retrieval: ", e);
			throw new RuntimeException("Exception during Google token retrieval", e);
		}

	}

	/**
	 * AccessToken 받은 후 user 정보를 요청하는 API
	 * userInfo 를 받은 경우, 해당하는 id가 sso-user-index 에 있다면 update, 없다면 insert 수행
	 *
	 * @param accessToken : 전달받은 AccessToken
	 * @return 생성된 Token 정보
	 */
	@Override
	public String[] requestUserInfo(String accessToken) {

		final HttpHeaders headers = new HttpHeaders();

		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

		final HttpEntity<String> httpEntity = new HttpEntity<>(headers);
		JsonNode jsonNode;

		try {

			// GET 요청 전송
			ResponseEntity<String> responseEntity = restTemplate.exchange(GOOGLE_USER_INFO_URI, HttpMethod.GET,
				httpEntity, String.class);

			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				jsonNode = objectMapper.readTree(responseEntity.getBody());
			} else {
				logger.warn("Failed to retrieve Google user info: {}", responseEntity.getStatusCode());
				throw new RuntimeException("Failed to retrieve Google user info");
			}

		} catch (Exception e) {
			logger.warn("Exception during Google user info retrieval: ", e);
			throw new RuntimeException("Exception during Google user info retrieval", e);
		}

		assert jsonNode != null;
		String id = jsonNode.get("sub").asText();

		UserTableWithRelationshipAndPictureNmDTO user = userTableMapper.findById(id);

		if (null == user) {

			user = new UserTableWithRelationshipAndPictureNmDTO();

			user.setUserId(id);
			user.setName(jsonNode.get("name").asText());
			user.setEmail(jsonNode.get("email").asText());
			user.setNickName(new RandomNickNameMaker().makeRandomNickName());
			user.setLoginType(SocialLoginType.GOOGLE);

			userTableMapper.makeNewUser(user);

		} else {

			user.setName(jsonNode.get("name").asText());
			user.setEmail(jsonNode.get("email").asText());

			userTableMapper.updateUser(user);
		}

		String userRefreshToken = jwtTokenProvider.generateRefreshToken(user);
		String userAccessToken = jwtTokenProvider.generateAccessToken(user);

		logger.info("LOGIN : [{}]", user.getName());

		return new String[] {userRefreshToken, userAccessToken};
	}
}
