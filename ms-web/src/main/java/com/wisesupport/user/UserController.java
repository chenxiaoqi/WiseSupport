package com.wisesupport.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Optional;

/**
 * Author chenxiaoqi on 2018/12/23.
 */
@Controller
@RequestMapping("/user")
@Slf4j
@AllArgsConstructor
public class UserController {


    private UserMapper userMapper;

    @GetMapping("user_list")
    public String userList(Model model,@SessionAttribute User user) {
        model.addAttribute("users", userMapper.findAll());
        model.addAttribute("user",user);
        return "user_list";
    }

    @GetMapping("/delete_user")
    public String deleteUser(@RequestParam Integer id) {
        userMapper.deleteById(id);
        return "redirect:user_list";
    }

    @GetMapping("/update_user")
    public String updateUserPage(@RequestParam Integer id, Model model) {
        User user = Optional.of(userMapper.findById(id)).orElseThrow(() -> new IllegalArgumentException(id.toString()));
        model.addAttribute("user", user);
        return "update_user";
    }

    @PostMapping("/update_user")
    public String updateUser(@RequestParam Integer id, @RequestParam String account, @RequestParam String password, @RequestParam Locale locale) {
        User user = Optional.of(userMapper.findById(id)).orElseThrow(() -> new IllegalArgumentException(id.toString()));
        user.setAccount(account);
        user.setLocale(locale.toString());
        user.setPassword(password);
        userMapper.update(user);
        return "redirect:user_list";
    }
}
