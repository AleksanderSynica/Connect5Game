//package com.server.connect5;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
//import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//
//@Configuration
//public class RestConfiguration implements RepositoryRestConfigurer {
//
//    @Override
//    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration configuration,
//                                                     CorsRegistry corsRegistry) {
//
//        corsRegistry.addMapping("/**")
//                .allowedMethods("GET", "POST", "PUT");
//    }
//}
