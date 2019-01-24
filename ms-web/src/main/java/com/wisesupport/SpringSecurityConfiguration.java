package com.wisesupport;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 *  * <一句话功能简述>
 *   * <功能详细描述>
 *    *
 *     * @author c00286900
 *      * @version [版本号, 2019/1/23]
 *       * @see [相关类/方法]
 *        * @since [产品/模块版本]
 *         */
@Configuration
public class SpringSecurityConfiguration
{
    @Bean
    public UserDetailsService userDetailsService()
    {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        manager.createUser(
                User
                        .withUsername("c00286900")
                        .password(passwordEncoder.encode("cxq@444.com"))
                        .roles("ADMIN")
                        .build()
        );
        manager.createUser(
                User
                        .withUsername("user")
                        .password(passwordEncoder.encode("user"))
                        .roles("USER")
                        .build()
        );
        return manager;
    }

    @Order(99)
    private static class WiseSupportSecurityConfigurer extends WebSecurityConfigurerAdapter
    {
        @Override
        protected void configure(HttpSecurity http) throws Exception
        {
            http
                    .authorizeRequests()
                    .antMatchers("/statics/**","/swagger-resources/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .formLogin()
            ;
        }

        @Override
        public void configure(WebSecurity web)
        {

        }
    }

    @Bean
    public WebSecurityConfigurer<WebSecurity> webSecurityConfigurerAdapter()
    {
        return new WiseSupportSecurityConfigurer();
    }
}

