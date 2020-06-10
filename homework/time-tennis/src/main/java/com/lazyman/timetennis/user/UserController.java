package com.lazyman.timetennis.user;

import com.lazyman.timetennis.BusinessException;
import com.lazyman.timetennis.SessionWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@RestController
public class UserController {

    private UserMapper userMapper;

    private String superAdmin;

    public UserController(UserMapper userMapper, @Value("${wx.super-admin}") String superAdmin) {
        this.userMapper = userMapper;
        this.superAdmin = superAdmin;
    }

    @GetMapping("/users")
    public List<User> users(@SessionAttribute User user) {
        return userMapper.selectAll();
    }

    @PostMapping("/user/grant_vip")
    public void vip(@SessionAttribute User user, String openId) {
        switchVip(user, openId, true);
    }

    @PostMapping("/user/cancel_vip")
    public void cancelVip(@SessionAttribute User user, String openId) {
        switchVip(user, openId, false);
    }

    private void switchVip(User user, String openId, boolean flag) {
        if (!user.getAdmin()) {
            throw new BusinessException("需要管理员权限");
        }
        User u = new User();
        u.setOpenId(openId);
        u.setVip(flag);
        if (userMapper.updateByPrimaryKey(u) == 1) {
            SessionWatch.destroy(openId);
        }
    }

    @PostMapping("/user/grant_admin")
    public void grantAdmin(@SessionAttribute User user, String openId) {
        switchAdmin(user, openId, true);
    }

    @PostMapping("/user/cancel_admin")
    public void cancelAdmin(@SessionAttribute User user, String openId) {
        switchAdmin(user, openId, false);
    }

    private void switchAdmin(User user, String openId, boolean flag) {
        if (!user.getOpenId().equals(superAdmin)) {
            throw new BusinessException("需要超级管理员权限");
        }
        User u = new User();
        u.setOpenId(openId);
        u.setAdmin(flag);
        if (userMapper.updateByPrimaryKey(u) == 1) {
            SessionWatch.destroy(openId);
        }
    }
}
