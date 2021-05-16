package com.coding.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class MyWebConfig implements WebMvcConfigurer {
    //跨域（未解决）
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //registry.addMapping("/*").allowedHeaders("*").allowedMethods("*").maxAge(1800).allowedOrigins("http://47.93.50.249:8080");
    }
}
