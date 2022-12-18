package com.barco.pipeline.config;

import com.barco.pipeline.engine.async.executor.AsyncDALTaskExecutor;
import com.barco.pipeline.engine.async.properties.AsyncTaskProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Nabeel Ahmed
 */
@Configuration
@ComponentScan(basePackages = "com.barco.*")
public class ProcessConfig {

    public Logger logger = LogManager.getLogger(ProcessConfig.class);

    @Autowired
    public AsyncTaskProperties asyncTaskProperties;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * This method use to create the asyncDALTaskExecutor bean
     * @return AsyncDALTaskExecutor
     */
    @Bean
    @Scope("singleton")
    public AsyncDALTaskExecutor asyncDALTaskExecutor() throws Exception {
        logger.debug("===============Application-DAO-INIT===============");
        AsyncDALTaskExecutor taskExecutor = new AsyncDALTaskExecutor(
            this.asyncTaskProperties.getMinThreads(), this.asyncTaskProperties.getMaxThreads(),
            this.asyncTaskProperties.getIdleThreadLife());
        logger.debug("===============Application-DAO-END===============");
        return taskExecutor;
    }

}
