package com.flowcore.workflow.auth.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                .anyRequest().authenticated()
        );

        http.formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler((request, response, authentication) ->{
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                })
                .failureHandler((request, response, exception) ->{
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                })
                .permitAll()
        );

        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request ,response , exception) ->{
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            })
        );

        http.logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request ,response , authentication) ->{
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                })
        );

        return http.build();
    }
}
