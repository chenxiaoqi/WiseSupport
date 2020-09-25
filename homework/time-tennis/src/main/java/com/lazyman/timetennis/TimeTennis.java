package com.lazyman.timetennis;

import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.user.UserCoder;
import com.wisesupport.commons.exceptions.LoginTimeoutException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

@SpringBootApplication(scanBasePackages = {"com.lazyman.timetennis", "com.wisesupport.commons"})
@ImportResource("classpath:/spring/applicationContext.xml")
@PropertySource("classpath:password.properties")
@ServletComponentScan(basePackageClasses = TimeTennis.class)
@EnableScheduling
@EnableTransactionManagement(proxyTargetClass = true)
@EnableCaching
public class TimeTennis {

    @Bean
    public WebMvcConfigurer webMvcConfigurer(UserCoder coder) {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new AbstractNamedValueMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(@NonNull MethodParameter parameter) {
                        return parameter.getParameterType() == User.class;
                    }

                    @Override
                    @NonNull
                    protected NamedValueInfo createNamedValueInfo(@NonNull MethodParameter parameter) {
                        return new NamedValueInfo(Objects.requireNonNull(parameter.getParameterName()), true, ValueConstants.DEFAULT_NONE);
                    }

                    @Override
                    protected Object resolveName(@NonNull String name, @NonNull MethodParameter parameter, @NonNull NativeWebRequest request) {
                        return coder.decode(Objects.requireNonNull(request.getNativeRequest(HttpServletRequest.class)));
                    }

                    @Override
                    protected void handleMissingValue(String name, MethodParameter parameter) {
                        throw new LoginTimeoutException();
                    }
                });
            }
        };
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .registerShutdownHook(true)
                .sources(TimeTennis.class)
                .run(args).registerShutdownHook();
    }
}
