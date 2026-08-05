package com.andrew.api_gateway.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class DeviceServiceRoutes {

    @Value("${services.device.url}")
    private String deviceServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> deviceRoute() {
        return route("device-service")
                .route(RequestPredicates.path("/api/v1/device/**"), http())
                .before(uri(deviceServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> deviceFallBackRoute() {
        return route("fallbackRoute")
                .route(RequestPredicates.path("/fallbackRoute"),
                        request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body("Device Service is down, please try again later."))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> deviceServiceApiDocs() {
        return GatewayRouterFunctions.route("deviceServiceApiDocs")
                .route(RequestPredicates.path("/docs/device-service/v3/api-docs"), http())
                .before(uri(deviceServiceUrl))
                .filter(setPath("/v3/api-docs"))
                .build();
    }
}