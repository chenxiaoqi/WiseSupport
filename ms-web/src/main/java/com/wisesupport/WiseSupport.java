package com.wisesupport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Author chenxiaoqi on 2018/12/22.
 */

@SpringBootApplication(scanBasePackages = "com.wisesupport")
@ImportResource("classpath:/spring/applicationContext.xml")
@EnableAuthorizationServer
public class WiseSupport implements WebMvcConfigurer {

    @Bean
    public AuthorizationServerConfigurer authorizationServerConfigurers(WebSecurityConfigurerAdapter webSecurityConfigurerAdapter) {
        return new AuthorizationServerConfigurerAdapter() {
            @Override
            public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
                security.passwordEncoder(new PasswordEncoder() {
                    @Override
                    public String encode(CharSequence rawPassword) {
                        return rawPassword.toString();
                    }

                    @Override
                    public boolean matches(CharSequence rawPassword, String encodedPassword) {
                        return rawPassword.toString().equals(encodedPassword);
                    }
                });
            }

            @Override
            public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
                endpoints.authenticationManager(webSecurityConfigurerAdapter.authenticationManagerBean());
            }


            @Override
            public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
                clients.inMemory()
                        .withClient("client")
                        .secret("123456")
                        .scopes("read")
                        .authorizedGrantTypes("client_credentials");

            }
        };
    }

    @Bean
    public WebSecurityConfigurerAdapter webSecurityConfigurerAdapter() {
        return new WebSecurityConfigurerAdapter() {
            @Override
            public void configure(HttpSecurity httpSecurity) throws Exception {
                httpSecurity
                        .formLogin()
                        .and()
                        .csrf()
                        .disable();
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(WiseSupport.class, args);
    }
}
