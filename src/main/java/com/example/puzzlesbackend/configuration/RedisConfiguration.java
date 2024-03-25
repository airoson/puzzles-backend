package com.example.puzzlesbackend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfiguration {
    @Value("${REDIS_HOST}")
    private String redisHost;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(){
        return new JedisConnectionFactory(new RedisStandaloneConfiguration(redisHost == null ? "localhost": redisHost, 6379));
    }

    @Bean
    public RedisTemplate redisTemplate(JedisConnectionFactory jedisConnectionFactory){
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }

}
