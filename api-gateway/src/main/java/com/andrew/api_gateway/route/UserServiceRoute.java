package com.andrew.api_gateway.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class UserServiceRoute{

    @Value("${services.user.url}")
    private String userServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> userRoute() {
        return route("user-service")
                .route(RequestPredicates.path("/api/v1/user/**"), http())
                .before(uri(userServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "userServiceCircuitBreaker",
                        URI.create("forward:/fallbackRoute")
                ))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userFallBackRoute() {
        return route("fallbackRoute")
                .route(RequestPredicates.path("/fallbackRoute"),
                        request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("User Service is down, please try again later."))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceApiDocs() {
        return GatewayRouterFunctions.route("userServiceApiDocs")
                .route(RequestPredicates.path("/docs/user-service/v3/api-docs"), http())
                .before(uri(userServiceUrl))
                .filter(setPath("/v3/api-docs"))
                .build();
    }
}
