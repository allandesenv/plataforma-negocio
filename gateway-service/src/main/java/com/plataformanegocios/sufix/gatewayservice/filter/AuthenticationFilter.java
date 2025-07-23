package com.plataformanegocios.sufix.gatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class AuthenticationFilter implements GatewayFilter {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        // Garante que a chave secreta tem o tamanho mínimo de 256 bits (32 bytes)
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private boolean isJwtValid(String jwt) {
        try {
            // Tenta parsear e validar o token
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(jwt);
            // Verifica a expiração explicitamente (JJWT 0.11.x pode não lançar exceção automática de expiração)
            return !isTokenExpired(jwt);
        } catch (Exception e) {
            System.err.println("Erro na validação JWT: " + e.getMessage());
            return false;
        }
    }

    // Métodos auxiliares para extrair claims, similares ao JwtUtil do auth-service
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
        // Aqui você pode extrair informações do JWT (como username, roles) e adicioná-las aos headers da requisição
        // para que os microsserviços downstream possam acessá-las.
        // Por exemplo:
        String username = extractUsername(token);
        exchange.getRequest().mutate()
                .header("X-Auth-Username", username)
                .build();
        // Futuramente, pode adicionar roles, user ID, etc.
    }

    // A lógica do filtro
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Lista de URLs que não exigem autenticação (pode vir do Config Server no futuro)
        // Por exemplo, /auth/** para login e registro
        List<String> openApiEndpoints = List.of("/auth/login", "/auth/register", "/eureka", "/swagger-ui", "/v3/api-docs"); // Adicione mais se necessário

        // Verifica se a rota é pública
        if (openApiEndpoints.stream().anyMatch(uri -> request.getURI().getPath().contains(uri))) {
            return chain.filter(exchange); // Continua a cadeia sem autenticação
        }

        // Se a requisição não tem cabeçalho de Autorização, ou não começa com "Bearer "
        if (!request.getHeaders().containsKey("Authorization") || !request.getHeaders().getFirst("Authorization").startsWith("Bearer ")) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete(); // Completa a requisição com 401
        }

        // Extrai o token
        String jwt = request.getHeaders().getFirst("Authorization").substring(7);

        // Valida o token
        if (!isJwtValid(jwt)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete(); // Token inválido, retorna 401
        }

        // Se o token é válido, popula a requisição com headers de autenticação
        populateRequestWithHeaders(exchange, jwt);

        // Continua a cadeia de filtros
        return chain.filter(exchange);
    }
}