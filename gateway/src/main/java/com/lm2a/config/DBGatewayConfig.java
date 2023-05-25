package com.lm2a.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DBGatewayConfig {
	
	@Autowired
	AuthFilter authFilter;
	
	@Bean
	@Profile("routing-no-eureka")
	public RouteLocator configLocalNoEureka(RouteLocatorBuilder builder) {
		return builder
				.routes()
				.route(r->r.path("/api/v1/dragonball/*").uri("http://localhost:8083"))
				.route(r->r.path("/api/v1/gameofthrones/*").uri("http://localhost:8088"))
				.build();
	}
	
	
	@Bean
	@Profile("routing-eureka")
	public RouteLocator configLocalEureka(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r->r.path("/api/v1/dragonball/*").uri("lb://db"))
				.route(r->r.path("/api/v1/gameofthrones/*").uri("lb://got"))
				.build();
	}
	
	@Bean
	@Profile("routing-eureka-cb")
	public RouteLocator configLocalEurekaCircuitBreaker(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r->r.path("/api/v1/dragonball/*")
						//definicion del procesamiento del fallo en el servicio original
						.filters(f->f.circuitBreaker(c->c.setName("failoverCB")
						.setFallbackUri("forward:/api/v1/db-failover/dragonball/characters") //(*)
						.setRouteId("dbFailover")))
						.uri("lb://db"))
				
				.route(r->r.path("/api/v1/gameofthrones/*")
						.filters(f->f.filter(authFilter))
						.uri("lb://got"))
				
				.route(r->r.path("/api/v1/db-failover/dragonball/*").uri("lb://db-failover")) //(*)
				.build();

	}
	
}
