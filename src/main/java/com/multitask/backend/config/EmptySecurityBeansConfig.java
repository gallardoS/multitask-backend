package com.multitask.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@Profile("prod")
public class EmptySecurityBeansConfig {

    @Bean
    public UserDetailsService emptyDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("No local users");
        };
    }
}
