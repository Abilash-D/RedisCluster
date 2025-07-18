package com.abilash.bofa.POCForRedis;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import com.abilash.bofa.POCForRedis.config.LocalRedisInitializer;

@SpringBootTest
@ComponentScan(basePackages = "io.github.truongbn.redistestcontainers")
@ConfigurationPropertiesScan(basePackages = "io.github.truongbn.redistestcontainers")
class RedisAppRunner {
    public static void main(String[] args) {
        new SpringApplicationBuilder(RedisAppRunner.class).initializers(new LocalRedisInitializer())
                .run(args);
    }
}
