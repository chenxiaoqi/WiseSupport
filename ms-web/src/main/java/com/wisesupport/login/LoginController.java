package com.wisesupport.login;

import com.wisesupport.user.User;
import com.wisesupport.user.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Optional;

/**
 * Author chenxiaoqi on 2019-01-11.
 */
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController {

    private UserMapper userMapper;

    private LocaleResolver localeResolver;

    @GetMapping(path = "/sign_in")
    public String signInPage() {
        return "sign_in";
    }

    @PostMapping("/sign_in")
    public String signIn(@RequestParam String account, @RequestParam String password, Model model, HttpServletRequest request, HttpServletResponse response) {
        Optional<User> user = Optional.ofNullable(userMapper.findByAccount(account));
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            localeResolver.setLocale(request, response, StringUtils.parseLocale(user.get().getLocale()));
            return "redirect:/user/user_list";
        } else {
            model.addAttribute("errorMessage", "name or password incorrect.");
            return "sign_in";
        }

    }

    @GetMapping(path = "/sign_up")
    public String signUpPage() {
        return "sign_up";
    }

    @PostMapping(path = "/sign_up")
    public String signUp(@RequestParam String account, @RequestParam String password, @RequestParam Locale locale) {
        User user = new User();
        user.setAccount(account);
        user.setPassword(password);
        user.setLocale(locale.toString());
        userMapper.insert(user);
        return "redirect:sign_in";
    }

}
