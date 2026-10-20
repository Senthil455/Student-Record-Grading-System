package com.example.studentrecord.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Application beans.
 *
 * <p>This class also demonstrates dependency injection at the configuration
 * level: Spring calls these methods and manages the returned objects as beans
 * inside the ApplicationContext.</p>
 */
@Configuration
public class AppConfig {

    /**
     * HTTP client used to call the grading microservice. Short timeouts keep the
     * main application responsive when the second service is not running.
     */
    @Bean
    public RestClient gradingRestClient(RestClient.Builder builder,
                                        @Value("${grading.service.base-url:http://localhost:8081}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1500);
        requestFactory.setReadTimeout(2500);

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
