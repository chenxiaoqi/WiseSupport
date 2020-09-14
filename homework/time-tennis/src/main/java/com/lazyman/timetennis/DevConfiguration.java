package com.lazyman.timetennis;

import com.lazyman.timetennis.user.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Configuration
@Profile("!production")
public class DevConfiguration {

    @Bean
    public WebMvcConfigurer webMvcConfigurerAdapter() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                        HttpSession session = request.getSession(true);
                        User user = (User) session.getAttribute(Constant.SK_USER);
                        if (user == null) {
                            user = new User();
                            user.setOpenId("ows7_45zGxeOWXIUbKcPVul-2nqQ");
                            user.setWxNickname("dummy");
                            user.setAccountant(true);
                            user.setVip(true);
                            user.setSuperAdmin(true);
                            user.setArenaAdmin(true);
                            user.setAvatar("https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTLjkGHQyGTY0odH1vpklJl2MR6yGZmVodH1bx8tCJ6YNMGJUhHx4grkm9uuqaXDhUt2kuicGFeKAXQ/132");
                            session.setAttribute(Constant.SK_USER, user);
                        }
                        return true;
                    }
                }).excludePathPatterns("/statics/**");
            }
        };
    }
}
