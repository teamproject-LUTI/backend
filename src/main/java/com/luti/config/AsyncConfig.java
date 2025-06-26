package com.luti.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 처리 설정
 * 이메일 전송용 전용 스레드 풀을 구성합니다.
 */
@Slf4j
@Configuration
public class AsyncConfig {

	/**
	 * 이메일 전송용 비동기 Executor 설정
	 *
	 * @return ThreadPoolTaskExecutor 이메일 전송 전용 스레드 풀
	 */
	@Bean(name = "emailTaskExecutor")
	public Executor emailTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		// 기본 스레드 수 (항상 유지되는 스레드)
		executor.setCorePoolSize(3);

		// 최대 스레드 수 (피크 시간대 처리용)
		executor.setMaxPoolSize(10);

		// 큐 용량 (대기열 크기)
		executor.setQueueCapacity(50);

		// 스레드 이름 접두사
		executor.setThreadNamePrefix("Email-Async-");

		// 큐가 가득 찰 때의 정책 (호출자 스레드에서 실행)
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		// 스레드 풀 종료 시 작업 완료 대기 시간
		executor.setAwaitTerminationSeconds(20);

		// 애플리케이션 종료 시 작업 완료 대기
		executor.setWaitForTasksToCompleteOnShutdown(true);

		// 스레드 풀 초기화
		executor.initialize();

		log.info("이메일 전송용 비동기 스레드 풀 초기화 완료 - CoreSize: {}, MaxSize: {}, QueueCapacity: {}",
				executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

		return executor;
	}
}
