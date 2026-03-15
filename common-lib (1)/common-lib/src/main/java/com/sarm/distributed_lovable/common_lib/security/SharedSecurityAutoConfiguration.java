package com.sarm.distributed_lovable.common_lib.security;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SharedSecurityAutoConfiguration {

    @Bean
    public  AuthUtil authUtil(){
        return  new AuthUtil();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(AuthUtil authUtil , @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver){
        return new JwtAuthFilter(authUtil,handlerExceptionResolver);
    }

/*
    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if(attributes == null){
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            String authorization = request.getHeader("Authorization");

            System.out.println("Feign interceptor executed");
            System.out.println("Authorization header = " + authorization);

            if(authorization != null){
                requestTemplate.header("Authorization", authorization);
            }
        };
    }
*/

    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            System.out.println("Feign interceptor executed");
            if(authentication != null && authentication.getCredentials() instanceof String token){
                System.out.println("Token = " + token);
                requestTemplate.header("Authorization","Bearer "+token);
            }
        };
    }


}
