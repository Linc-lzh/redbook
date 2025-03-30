package com.shop.cart.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 这里使用单节点配置，您可以根据需要配置集群，哨兵等模式
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        // 如果有密码
        // .setPassword("yourPassword");

        return Redisson.create(config);
    }
}

