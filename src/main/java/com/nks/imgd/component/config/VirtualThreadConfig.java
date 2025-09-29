package com.nks.imgd.component.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class VirtualThreadConfig {

	@Bean("IMGD_VirtualThreadExecutor")
	public Executor virtualThreadExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}
}
