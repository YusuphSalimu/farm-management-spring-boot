package com.farm.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded profile pictures
        String uploadPath = System.getProperty("user.dir")
                + "/src/main/resources/static/uploads/profiles/";

        registry.addResourceHandler("/uploads/profiles/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
