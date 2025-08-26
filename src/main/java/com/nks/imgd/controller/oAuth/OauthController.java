package com.nks.imgd.controller.oAuth;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nks.imgd.component.config.JwtTokenProvider;
import com.nks.imgd.component.util.maker.TokenMaker;
import com.nks.imgd.dto.Enum.SocialLoginType;
import com.nks.imgd.dto.user.UserTableDTO;
import com.nks.imgd.service.oAuth.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote SSO Login 시 인입되는 페이지
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class OauthController {

	private static final Logger logger = LoggerFactory.getLogger(OauthController.class);
	private final AuthService authService;
	private final JwtTokenProvider jwtTokenProvider;
	private final TokenMaker tokenMaker;

	/**
	 * SSO LOGIN 시도 시 인입되는 페이지. 각 요청 별 enum 타입으로 Service request 시행
	 *
	 * @param socialLoginType : 로그인할 SSO Type (Google, NAVER, KAKAO)
	 */
	@GetMapping(value = "/{socialLoginType}")
	public ResponseEntity<Map<String, String>> socialLoginType(
		@PathVariable(name = "socialLoginType") SocialLoginType socialLoginType) {

		return ResponseEntity.ok()
			.body(authService.request(socialLoginType));
	}

	/**
	 * SSO 요청 후 Refresh 받는 callback Controller
	 * 각 SocialLoginType 별 반환 값을 받은 뒤 service 계층에 전달한다.
	 *
	 * @param socialLoginType     : 로그인할 SSO Type (Google, NAVER, KAKAO)
	 * @param authorizationCode   : SSO 요청 후 받은 반환 값인 verify code
	 * @param httpServletResponse : 반환 될 response 객체
	 * @return token : response 객체에 refresh 토큰 담아 반환
	 */
	@PostMapping(value = "/login/{socialLoginType}/callback")
	@ResponseBody
	@CrossOrigin(origins = "http://localhost:8081")
	public ResponseEntity<Map<String, Object>> callback(
		@PathVariable(name = "socialLoginType") SocialLoginType socialLoginType,
		@RequestBody Map<String, String> authorizationCode, HttpServletResponse httpServletResponse) {

		String[] tokens;
		String code = authorizationCode.get("authorizationCode");

		tokens = authService.requestAccessToken(socialLoginType, code /*, redirectUri*/);
		// if (socialLoginType.equals(SocialLoginType.GOOGLE)) {
		// 	tokens = authService.requestUserInfo(authorizationCode.get("authorizationCode"));
		// } else {
		// 	tokens = authService.requestAccessToken(socialLoginType, authorizationCode.get("authorizationCode"));
		// }

		// refreshToken
		String refreshTokenValue = tokens[0];
		String accessTokenValue = tokens[1];

		Cookie refreshTokenForKnock = new Cookie("refreshTokenForKnock", refreshTokenValue);
		Cookie accessToken = new Cookie("accessToken", accessTokenValue);

		tokenMaker.makeRefreshToken(httpServletResponse, refreshTokenForKnock);
		tokenMaker.makeAccessToken(accessToken);

		String redirectUrl = "/";

		Map<String, Object> response = new HashMap<>();
		response.put("redirectUrl", redirectUrl);
		// response.put("accessToken", accessToken);
		response.put("accessToken", accessTokenValue);

		return ResponseEntity.ok(response);

	}

	/**
	 * 프론트로부터 refreshToken 받아 accessToken 반환한다.
	 *
	 * @return token : response 객체에 access 토큰 담아 반환
	 */
	@PostMapping(value = "/getAccessToken")
	@ResponseBody
	@CrossOrigin(origins = "http://localhost:8081")
	public ResponseEntity<Map<String, String>> getAccessToken(HttpServletRequest request) {

		String token = jwtTokenProvider.resolveToken(request);

		try {
			UserTableDTO user = jwtTokenProvider.getUserDetails(token);
			String accessToken = jwtTokenProvider.generateAccessToken(user);

			Cookie accessTokenForKnock = new Cookie("accessToken", accessToken);

			tokenMaker.makeAccessToken(accessTokenForKnock);

			String redirectUrl = "/";

			Map<String, String> response = new HashMap<>();
			response.put("redirectUrl", redirectUrl);
			response.put("accessToken", accessToken);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.warn("AccessToken 생성 중 에러 발생 {}", e.getMessage());
			return ResponseEntity.badRequest().build();
		}

	}

	/**
	 * 프론트로부터 refreshToken 받아 제거한다.
	 */
	@PostMapping(value = "/logout")
	@ResponseBody
	@CrossOrigin(origins = "http://localhost:8081")
	public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		try {
			if (auth != null) {
				new SecurityContextLogoutHandler().logout(request, response, auth);
			}

			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("refreshTokenForKnock")) {
						tokenMaker.makeTokenValidOut(cookie);
						response.addCookie(cookie);
					}
				}
			}

			return ResponseEntity.ok().build();

		} catch (Exception e) {
			logger.warn("토큰 제거 중 에러 발생 {}", e.getMessage());
			return ResponseEntity.badRequest().build();
		}

	}
}
