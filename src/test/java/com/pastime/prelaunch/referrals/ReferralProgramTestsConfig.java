package com.pastime.prelaunch.referrals;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.pastime.prelaunch.referrals.ReferralProgram;

@Configuration
public class ReferralProgramTestsConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        return factory;
    }
    
    @Bean
    public StringRedisTemplate redisTemplate() {
        return new StringRedisTemplate(redisConnectionFactory());
    }
    
    @Bean
    public ReferralProgram referralProgram() {
        return new ReferralProgram(redisTemplate());
    }
    
}
