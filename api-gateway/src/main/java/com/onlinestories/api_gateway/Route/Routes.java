package com.onlinestories.api_gateway.Route;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.*;

@Configuration
public class Routes {
    @Bean
    public RouterFunction<ServerResponse> userRoutes(){
        return GatewayRouterFunctions.route("user-service")
                .route(RequestPredicates.path("/api/users/**"),
                        HandlerFunctions.http("http://localhost:8081"))
                        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> authRoutes(){
        return GatewayRouterFunctions.route("authentication-service")
                .route(RequestPredicates.path("/api/auth/**"),
                        HandlerFunctions.http("http://localhost:8082"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> storyRoutes(){
        return GatewayRouterFunctions.route("story-service")
                .route(RequestPredicates.path("/api/stories/**"),
                        HandlerFunctions.http("http://localhost:8083"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> commentRoutes(){
        return GatewayRouterFunctions.route("media-service")
                .route(RequestPredicates.path("/api/media/**"),
                        HandlerFunctions.http("http://localhost:8084"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> transRoutes(){
        return GatewayRouterFunctions.route("transaction-service")
                .route(RequestPredicates.path("/api/transactions/**"),
                        HandlerFunctions.http("http://localhost:8085"))
                .build();
    }
}
