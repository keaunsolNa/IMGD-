package com.nks.imgd.component.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecretKeyConfig {

	@Value("${jwt.secret}")
	private String secret;

	@Bean
	public SecretKey jwtSecretKey() {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		return new SecretKeySpec(keyBytes, "HmacSHA256");
	}
}
