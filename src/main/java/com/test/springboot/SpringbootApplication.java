package com.test.springboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.test.springboot.mapper")
public class SpringbootApplication extends SpringBootServletInitializer {

    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // 设置启动类,用于独立tomcat运行的入口
        return builder.sources(SpringbootApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }
}
