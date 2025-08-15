package com.nks.imgd.component.config;

import com.nks.imgd.component.util.maker.KeyMaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;

@Configuration
public class JwtDecoderConfig {

    /**
     * SecurityFilterChain 이 요구하는 Decoder Bean
     *
     * @return decoding 된 정보
     */
    @Bean
    public NimbusJwtDecoder jwtDecoder() {

        KeyMaker keyMaker = new KeyMaker();
        SecretKey key = keyMaker.generateKey();

        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
