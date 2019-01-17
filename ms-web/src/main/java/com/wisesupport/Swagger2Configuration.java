package com.wisesupport;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.common.base.Predicates;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static springfox.documentation.builders.PathSelectors.ant;

/**
 * Author chenxiaoqi on 2019-01-18.
 */
@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
@ConditionalOnProperty(prefix = "swagger", name = "enabled", havingValue = "true")
public class Swagger2Configuration {
    @Bean
    public Module jacksonAfterBurnerModule() {
        return new AfterburnerModule();
    }

    @Bean
    public HttpMessageConverter httpSmileJackson2MessageConverter() {
        return new AbstractJackson2HttpMessageConverter(
                new ObjectMapper(new SmileFactory()).registerModule(new AfterburnerModule()),
                new MediaType("application", "x-jackson-smile")) {
        };
    }

    @Bean
    public Docket restApi() {

        List<Parameter> pars = new ArrayList<Parameter>();
        ParameterBuilder authorizationHeader = new ParameterBuilder();
        authorizationHeader.name("Authorization").description("auth header")
                .modelRef(new ModelRef("string")).parameterType("header")
                .required(false).build();

        pars.add(authorizationHeader.build());

        return new Docket(DocumentationType.SWAGGER_2)
                .globalOperationParameters(pars)
                .apiInfo(apiInfo())
                .securitySchemes(asList(
                        new OAuth(
                                "ms_web_auth",
                                asList(new AuthorizationScope("write", "modify pets in your account"),
                                        new AuthorizationScope("read", "read your pets")),
                                Collections.singletonList(new ImplicitGrant(new LoginEndpoint("http://www.webo.com/login"), "tokenName"))
                        ),
                        new ApiKey("Authorization", "Authorization", "header"),
                        new BasicAuth("username", asList(new StringVendorExtension("username", "password")))

                ))
                .select()
                .paths(
                        Predicates.and(
                                ant("/**"),
                                Predicates.not(ant("/error")),
                                Predicates.not(ant("/actuator/**"))
                        )
                )
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Swagger Petstore")
                .description("Petstore API Description")
                .contact(new Contact("TestName", "http:/test-url.com", "test@test.de"))
                .license("Apache 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .version("1.0.0")
                .build();
    }
}
