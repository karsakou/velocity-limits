package com.ihar.velocity.limits.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VelocityLimitsProperties.class)
public class VelocityLimitsConfiguration {
}
