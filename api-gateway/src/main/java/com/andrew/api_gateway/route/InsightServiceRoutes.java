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
public class InsightServiceRoutes {

    @Value("${services.insight.url}")
    private String insightServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> insightRoute() {
        return route("insight-service")
                .route(RequestPredicates.path("/api/v1/insight/**"), http())
                .before(uri(insightServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> insightFallBackRoute() {
        return route("fallbackRoute")
                .route(RequestPredicates.path("/fallbackRoute"),
                        request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body("Insight Service is down, please try again later."))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> insightServiceApiDocs() {
        return GatewayRouterFunctions.route("insightServiceApiDocs")
                .route(RequestPredicates.path("/docs/insight-service/v3/api-docs"), http())
                .before(uri(insightServiceUrl))
                .filter(setPath("/v3/api-docs"))
                .build();
    }
}
