package com.example.feignpatchfix.config;

import feign.Client;
import okhttp3.ConnectionPool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration(proxyBeanMethods = false)
public class OkHttpFeignConfiguration {

    @Bean
    @ConditionalOnMissingBean(ConnectionPool.class)
    public ConnectionPool httpClientConnectionPool(FeignHttpClientProperties httpClientProperties) {
        int maxTotalConnections = httpClientProperties.getMaxConnections();
        long timeToLive = httpClientProperties.getTimeToLive();
        TimeUnit ttlUnit = httpClientProperties.getTimeToLiveUnit();
        return new ConnectionPool(maxTotalConnections, timeToLive, ttlUnit);
    }


    @Bean
    public Client feignClient() {
        return new OkHttpClient();
    }

}
