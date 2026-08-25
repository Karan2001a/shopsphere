package com.shopsphere.apigateway.filter;

import com.shopsphere.apigateway.security.JwtService;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter
        implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(
            JwtService jwtService) {

        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String path =
                exchange.getRequest()
                        .getURI()
                        .getPath();

        /*
         * These endpoints do not require JWT.
         */
        if (isPublicEndpoint(path)) {

            return chain.filter(exchange);
        }

        String authorizationHeader =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(
                                HttpHeaders.AUTHORIZATION
                        );

        /*
         * No Authorization header
         */
        if (authorizationHeader == null
                ||
                !authorizationHeader
                        .startsWith("Bearer ")) {

            return unauthorized(
                    exchange,
                    "Missing or invalid Authorization header"
            );
        }

        String token =
                authorizationHeader.substring(7);

        /*
         * Validate JWT signature + expiration.
         */
        if (!jwtService.isTokenValid(token)) {

            return unauthorized(
                    exchange,
                    "Invalid or expired JWT token"
            );
        }

        /*
         * Extract user email from JWT.
         */
        String email =
                jwtService.extractEmail(token);

        /*
         * Add authenticated email to downstream request.
         *
         * Later services can read:
         *
         * X-Authenticated-User
         */
        ServerWebExchange mutatedExchange =
                exchange.mutate()
                        .request(
                                exchange.getRequest()
                                        .mutate()
                                        .header(
                                                "X-Authenticated-User",
                                                email
                                        )
                                        .build()
                        )
                        .build();

        return chain.filter(
                mutatedExchange
        );
    }

    private boolean isPublicEndpoint(
            String path) {

        return path.equals(
                "/api/users/register"
        )
                ||
                path.equals(
                        "/api/users/login"
                );
    }

    private Mono<Void> unauthorized(
            ServerWebExchange exchange,
            String message) {

        exchange.getResponse()
                .setStatusCode(
                        HttpStatus.UNAUTHORIZED
                );

        exchange.getResponse()
                .getHeaders()
                .setContentType(
                        MediaType.APPLICATION_JSON
                );

        String json =
                """
                {
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "%s"
                }
                """.formatted(message);

        byte[] bytes =
                json.getBytes(
                        StandardCharsets.UTF_8
                );

        return exchange.getResponse()
                .writeWith(
                        Mono.just(
                                exchange.getResponse()
                                        .bufferFactory()
                                        .wrap(bytes)
                        )
                );
    }

    @Override
    public int getOrder() {

        /*
         * Run early in Gateway filter chain.
         */
        return -1;
    }
}