package com.lazyman.timetennis.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lazyman.timetennis.arena.ArenaPrivilege;
import com.lazyman.timetennis.core.WeXinService;
import com.lazyman.timetennis.core.WeXinToken;
import com.lazyman.timetennis.privilege.RoleDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;

@RestController
@Slf4j
@Validated
public class LoginController {

    private UserMapper userMapper;

    private RoleDao roleDao;

    private ArenaPrivilege arenaPrivilege;

    private UserCoder coder;

    private WeXinService weXinService;

    public LoginController(UserMapper userMapper,
                           RoleDao roleDao,
                           ArenaPrivilege arenaPrivilege, UserCoder coder,
                           WeXinService weXinService) {
        this.userMapper = userMapper;
        this.roleDao = roleDao;
        this.arenaPrivilege = arenaPrivilege;
        this.coder = coder;
        this.weXinService = weXinService;
    }

    @GetMapping("/login_v2")
    public User loginV2(@RequestParam @NotEmpty String jsCode, HttpServletResponse resp) throws IOException {
        WeXinToken token = weXinService.getWeXinToken(jsCode);
        User user = userMapper.selectByPrimaryKey(token.getOpenId());
        if (user != null) {
            user.setSuperAdmin(roleDao.isSuperAdmin(token.getOpenId()));
            user.setArenaAdmin(arenaPrivilege.hasArena(token.getOpenId()));

        } else {
            user = new User();
            user.setOpenId(token.getOpenId());
        }
        coder.encode(user, resp);

        //没有昵称或者头像就不返回用户信息,界面就会提示用户登录
        if (StringUtils.isEmpty(user.getWxNickname()) || StringUtils.isEmpty(user.getAvatar())) {
            user = null;
        }
        return user;
    }

    @PostMapping("/register")
    public void register(User user,
                         @RequestParam @NotEmpty String name,
                         @RequestParam @NotEmpty String avatar) {
        User dbUser = userMapper.selectByPrimaryKey(user.getOpenId());
        if (dbUser == null) {
            dbUser = new User();
            user.setOpenId(user.getOpenId());
            user.setWxNickname(name);
            user.setAvatar(avatar);
            userMapper.insert(dbUser);
        } else {
            dbUser.setWxNickname(name);
            dbUser.setAvatar(avatar);
            userMapper.updateByPrimaryKey(dbUser);
        }
    }


    //todo 删除
    @GetMapping("/login")
    public User login(@RequestParam @NotEmpty String jsCode,
                      @RequestParam @NotEmpty String rawData,
                      @RequestParam @NotEmpty String signature,
                      HttpServletResponse resp) throws IOException {

        WeXinToken token = weXinService.getWeXinToken(jsCode);
        String expect = Hex.encodeHexString(DigestUtils.sha1(rawData + token.getSessionKey()));
        Assert.isTrue(expect.equals(signature), () -> "verify signature failed expect " + expect + " actual " + signature);

        User wxUser = new User();
        wxUser.setOpenId(token.getOpenId());

        JSONObject raw = JSON.parseObject(rawData);
        wxUser.setWxNickname(raw.getString("nickName"));
        wxUser.setAvatar(raw.getString("avatarUrl"));

        User result = userMapper.selectByPrimaryKey(wxUser.getOpenId());
        if (result == null) {
            userMapper.insert(wxUser);
            result = userMapper.selectByPrimaryKey(wxUser.getOpenId());
        }
        result.setSuperAdmin(roleDao.isSuperAdmin(result.getOpenId()));
        result.setArenaAdmin(arenaPrivilege.hasArena(result.getOpenId()));
        //保存到cookie里
        coder.encode(result, resp);

        return result;
    }

    @GetMapping("/logout")
    public void logout(User user) {
        userMapper.deregister(user.getOpenId());
    }
}
