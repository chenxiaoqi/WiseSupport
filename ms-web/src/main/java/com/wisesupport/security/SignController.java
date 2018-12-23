package com.wisesupport.security;

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
 * Author chenxiaoqi on 2018/12/23.
 */
@Controller
@RequestMapping("/security")
@Slf4j
@AllArgsConstructor
public class SignController {


    private UserRepository userRepository;

    private LocaleResolver localeResolver;


    @GetMapping(path = "/sign_in")
    public String signInPage() {
        return "sign_in";
    }

    @PostMapping("/sign_in")
    public String signIn(@RequestParam String name, @RequestParam String password, Model model, HttpServletRequest request, HttpServletResponse response) {
        Optional<User> user = userRepository.findByName(name);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            localeResolver.setLocale(request, response, StringUtils.parseLocale(user.get().getLocale()));
            return "redirect:user_list";
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
    public String signUp(@RequestParam String name, @RequestParam String password, @RequestParam Locale locale) {
        userRepository.save(User.of(name, password, locale.toString()));
        return "redirect:sign_in";
    }

    @GetMapping("user_list")
    public String userList(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "user_list";
    }

    @GetMapping("/delete_user")
    public String deleteUser(@RequestParam Long id) {
        userRepository.deleteById(id);
        return "redirect:user_list";
    }

    @GetMapping("/update_user")
    public String updateUserPage(@RequestParam Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(id.toString()));
        model.addAttribute("user", user);
        return "update_user";
    }

    @PostMapping("/update_user")
    public String updateUser(@RequestParam Long id, @RequestParam String name, @RequestParam String password, @RequestParam Locale locale) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(id.toString()));
        user.setName(name);
        user.setLocale(locale.toString());
        user.setPassword(password);
        userRepository.save(user);
        return "redirect:user_list";
    }
}
