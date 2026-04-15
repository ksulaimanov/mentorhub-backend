package kg.kut.os.mentorhub.common.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class RateLimitFilterConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitProperties properties) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter(properties));
        registration.addUrlPatterns("/api/auth/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("rateLimitFilter");
        return registration;
    }
}

