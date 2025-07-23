package com.plataformanegocios.sufix.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity // Habilita Spring Security para WebFlux
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Desabilita CSRF para APIs REST sem estado
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/**").permitAll() // Permite acesso a endpoints de auth (login, register)
                        .pathMatchers("/eureka/**").permitAll() // Permite acesso a endpoints do Eureka (para Eureka Server)
                        .pathMatchers("/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll() // Permite acesso ao Swagger UI
                        .anyExchange().authenticated() // Qualquer outra requisição exige autenticação
                );
        return http.build();
    }
}