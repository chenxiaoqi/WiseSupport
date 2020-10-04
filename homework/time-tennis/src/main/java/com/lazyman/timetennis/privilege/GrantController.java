package com.lazyman.timetennis.privilege;

import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.user.UserMapper;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@RequestMapping("/manage/privilege")
public class GrantController {

    private RoleDao roleDao;

    private UserMapper userMapper;

    public GrantController(RoleDao roleDao, UserMapper userMapper) {
        this.roleDao = roleDao;
        this.userMapper = userMapper;
    }

    @GetMapping("/arena_admins")
    public List<User> arenaAdmins(User user) {
        if (!user.isSuperAdmin()) {
            throw new BusinessException("需要超级管理员权限");
        }
        return roleDao.arenaAdmins();
    }

    @DeleteMapping("/role")
    public void revoke(User user, @RequestParam @NotEmpty String openId,
                       @RequestParam @NotEmpty String roleName) {
        if (!user.isSuperAdmin()) {
            throw new BusinessException("需要超级管理员权限");
        }
        Validate.isTrue(roleDao.revoke(openId, roleName) == 1);
    }

    @PostMapping("/role")
    public void grant(User user, @RequestParam @NotEmpty String openId,
                      @RequestParam @NotEmpty String roleName) {
        if (!user.isSuperAdmin()) {
            throw new BusinessException("需要超级管理员权限");
        }
        User u = userMapper.selectByPrimaryKey(openId);
        Validate.notNull(u, "openId %s not exists.", openId);
        try {
            roleDao.grant(openId, roleName);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该用户已有权限");
        }
    }

}
