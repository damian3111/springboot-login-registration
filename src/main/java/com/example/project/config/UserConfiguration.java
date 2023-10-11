package com.example.project.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAsync
@EnableCaching
@EnableTransactionManagement
@RequiredArgsConstructor
public class UserConfiguration {

    private final DefaultOAuth2UserService customOauth2UserService;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain securityFilterChain2(HttpSecurity httpSecurity) throws Exception {


        return httpSecurity
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .authorizeHttpRequests(r -> r
                        .anyRequest().authenticated())
                .oauth2Login(r -> r
                        .defaultSuccessUrl("/posts", true)
                        .userInfoEndpoint(t -> t.userService(customOauth2UserService)))
                .build();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(r -> r.requestMatchers("/sample/**", "/rest/**", "/login", "/login", "/register", "/reset", "/resetPassword", "/changePassword/**",  "/css/*", "/js/*", "/img/*", "/confirm/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(r -> r.loginPage("/login").permitAll()
                        .failureHandler(new CustomAuthenticationFailureHandler())
                        .defaultSuccessUrl("/posts", true))
                .logout(r -> r
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true))
                .build();
    }



}
