package com.nks.imgd.component.config;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtDecoderConfig {

	/**
	 * SecurityFilterChain 이 요구하는 Decoder Bean
	 *
	 * @return decoding 된 정보
	 */
	@Bean
	public NimbusJwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
		return NimbusJwtDecoder.withSecretKey(jwtSecretKey).build();
	}
}
