package com.lm2a.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.net.HttpHeaders;
import com.lm2a.dto.TokenDto;

import reactor.core.publisher.Mono;


@Component
public class AuthFilter implements GatewayFilter {

	@Autowired
	private WebClient.Builder webClient;
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		if(!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
			return onError(exchange, HttpStatus.BAD_REQUEST);
		}
		String tokenHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
		String[] chunks = tokenHeader.split(" ");
		if(chunks.length != 2 || !chunks[0].equals("Bearer")) {
			return onError(exchange, HttpStatus.BAD_REQUEST);
		}
		
		return webClient.build()
				.post().uri("http://ms-auth/auth/validate?token="+chunks[1])
				.retrieve()
				.bodyToMono(TokenDto.class)
				.map(t->exchange)
				.flatMap(chain::filter);
	}
	
	
	
	private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status){
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		return response.setComplete();
	}

}
