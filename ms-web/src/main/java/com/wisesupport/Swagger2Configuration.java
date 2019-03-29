package com.wisesupport;

import com.google.common.base.Predicates;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
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
    public Docket restApi() {

        List<Parameter> pars = new ArrayList<Parameter>();
        ParameterBuilder authorizationHeader = new ParameterBuilder();
        authorizationHeader.name("Authorization").description("auth header")
                .modelRef(new ModelRef("string")).parameterType("header")
                .required(true).build();

        pars.add(authorizationHeader.build());

        return new Docket(DocumentationType.SWAGGER_2)
                .globalOperationParameters(pars)
                .apiInfo(apiInfo())
                .pathMapping("/")
                .globalResponseMessage(RequestMethod.GET, asList(
                        new ResponseMessageBuilder()
                                .code(500).message("500 message")
                                .responseModel(new ModelRef("Person"))
                                .build()
                ))
                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.wisesupport"))
                .paths(
                        Predicates.and(
                                ant("/validate/**"),
                                Predicates.not(ant("/error")),
                                Predicates.not(ant("/actuator/**"))
                        )
                )
                .build()
                ;
    }

    @Bean
    UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .displayOperationId(false)
                .defaultModelsExpandDepth(1)
                .defaultModelExpandDepth(1)
                .defaultModelRendering(ModelRendering.EXAMPLE)
                .displayRequestDuration(false)
                .docExpansion(DocExpansion.NONE)
                .filter(false)
                .maxDisplayedTags(null)
                .operationsSorter(OperationsSorter.ALPHA)
                .showExtensions(false)
                .tagsSorter(TagsSorter.ALPHA)
                .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
//                .validatorUrl(null)
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("WiseSupport API Doc")
                .description("WiseSupport API Doc")
                .contact(new Contact("cxq", null, "test@test.de"))
                .license("Apache 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .version("1.0.0")
                .termsOfServiceUrl("http://term.wise.com")
                .build();
    }
}
