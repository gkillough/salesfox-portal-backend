package ai.salesfox.portal.common.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfiguration {
    public static final String DEFAULT_EXECUTOR_SERVICE_NAME = "portalMaxThreadPool";
    public static final String SINGLE_THREADED_EXECUTOR_SERVICE_NAME = "portalSingleThreadPool";

    @Bean(name = DEFAULT_EXECUTOR_SERVICE_NAME, destroyMethod = "shutdown")
    public ExecutorService maxThreadPoolExecutorService() {
        Runtime applicationRuntime = Runtime.getRuntime();
        return Executors.newFixedThreadPool(applicationRuntime.availableProcessors());
    }

    @Bean(name = SINGLE_THREADED_EXECUTOR_SERVICE_NAME, destroyMethod = "shutdown", autowireCandidate = false)
    public ExecutorService singleThreadPoolExecutorService() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(maxThreadPoolExecutorService());
        return eventMulticaster;
    }

}
