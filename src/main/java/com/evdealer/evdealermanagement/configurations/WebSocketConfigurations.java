package com.evdealer.evdealermanagement.configurations;

import com.evdealer.evdealermanagement.service.implement.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@Slf4j
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfigurations implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;


}
