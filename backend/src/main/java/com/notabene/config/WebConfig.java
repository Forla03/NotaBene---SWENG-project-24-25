package com.notabene.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // CORS configuration is handled in CorsConfig.java to avoid conflicts
    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/**")
    //         .allowedOrigins("http://localhost:3000")
    //         .allowedMethods("*")
    //         .allowedHeaders("*");
    // }
}
