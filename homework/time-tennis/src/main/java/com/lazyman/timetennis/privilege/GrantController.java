package com.lazyman.timetennis.privilege;

import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/privilege")
public class GrantController {

    private RoleDao roleDao;

    public GrantController(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    @GetMapping("/arena_admins")
    public List<User> arenaAdmins(@SessionAttribute User user) {
        if (!user.isSuperAdmin()) {
            throw new BusinessException("需要超级管理员权限");
        }
        return roleDao.arenaAdmins();
    }

    @DeleteMapping("/role")
    public void revoke(@SessionAttribute User user, String openId, String roleName) {
        if (!user.isSuperAdmin()) {
            throw new BusinessException("需要超级管理员权限");
        }
        Validate.isTrue(roleDao.revoke(openId, roleName) == 1);
    }

    @PostMapping("/role")
    public void grant(@SessionAttribute User user, String openId, String roleName) {
        if (!user.isSuperAdmin()) {
            throw new BusinessException("需要超级管理员权限");
        }
        try {
            roleDao.grant(openId, roleName);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该用户已有权限");
        }
    }

}
