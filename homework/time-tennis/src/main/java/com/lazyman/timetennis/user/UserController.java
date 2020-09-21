package com.lazyman.timetennis.user;

import com.lazyman.timetennis.SessionWatch;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.Validate;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
public class UserController {

    private UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    public List<User> users(String nickname) {
        return userMapper.query(nickname);
    }

    @GetMapping("/user/{openId}")
    public User user(@PathVariable @Size(min = 4, max = 64) String openId) {
        User u = userMapper.selectByPrimaryKey(openId);
        Validate.notNull(u);
        return u;
    }

    @PostMapping("/user/grant_vip")
    public void vip(@SessionAttribute User user, @RequestParam @NotEmpty @Size(min = 1, max = 64) String openId) {
        switchVip(user, openId, true);
    }

    @PostMapping("/user/cancel_vip")
    public void cancelVip(@SessionAttribute User user, @RequestParam @NotEmpty @Size(min = 1, max = 64) String openId) {
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
    public void grantAdmin(@SessionAttribute User user, @RequestParam @NotEmpty @Size(min = 1, max = 64) String openId) {
        switchAdmin(user, openId, true);
    }

    @PostMapping("/user/cancel_admin")
    public void cancelAdmin(@SessionAttribute User user, @RequestParam @NotEmpty @Size(min = 1, max = 64) String openId) {
        switchAdmin(user, openId, false);
    }

    private void switchAdmin(User user, String openId, boolean flag) {
        if (!user.isSuperAdmin()) {
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
