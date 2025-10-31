package com.evdealer.evdealermanagement.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfiguration implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-email-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable ex, Method method, Object... params) {
                log.error("╔════════════════════════════════════════════");
                log.error("║ ASYNC METHOD EXCEPTION CAUGHT");
                log.error("╠════════════════════════════════════════════");
                log.error("║ Method: {}", method.getName());
                log.error("║ Exception: {}", ex.getClass().getSimpleName());
                log.error("║ Message: {}", ex.getMessage());
                log.error("║ Parameters: ");
                for (int i = 0; i < params.length; i++) {
                    log.error("║   [{}] {}", i, params[i]);
                }
                log.error("╚════════════════════════════════════════════");
                log.error("Stack trace:", ex);

                // TODO: Có thể gửi alert đến Slack/Discord/Telegram
                // TODO: Có thể lưu vào database để retry sau
            }
        };
    }
}