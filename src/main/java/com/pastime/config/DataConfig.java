package com.pastime.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DataConfig {

	@Bean
	public DataSource dataSource() {
	    DriverManagerDataSource dataSource = new DriverManagerDataSource();
	    dataSource.setDriverClassName("org.postgresql.Driver");
	    dataSource.setUrl("jdbc:postgresql:pastime");
	    dataSource.setUsername("pastime");
	    dataSource.setPassword("hitmen");
	    return dataSource;
	}
	
	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource());
	}
	
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
	    JedisConnectionFactory factory = new JedisConnectionFactory();
	    return factory;
	}
	
	@Bean
	public StringRedisTemplate redisTemplate() {
	    return new StringRedisTemplate(redisConnectionFactory());
	}
	
}
