package com.lazyman.timetennis.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lazyman.timetennis.core.WeXinService;
import com.lazyman.timetennis.core.WeXinToken;
import com.lazyman.timetennis.privilege.RoleDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;

@RestController
@Slf4j
@Validated
public class LoginController {

    private UserMapper userMapper;

    private RoleDao roleDao;

    private UserCoder coder;

    private WeXinService weXinService;

    public LoginController(UserMapper userMapper,
                           RoleDao roleDao,
                           UserCoder coder,
                           WeXinService weXinService) {
        this.userMapper = userMapper;
        this.roleDao = roleDao;
        this.coder = coder;
        this.weXinService = weXinService;
    }

    @GetMapping("/login")
    public User login(@RequestParam @NotEmpty String jsCode,
                      @RequestParam @NotEmpty String rawData,
                      @RequestParam @NotEmpty String signature,
                      HttpServletRequest request, HttpServletResponse resp) throws IOException {

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
        } else {
            User loginUser = coder.decode(request);
            //用户登录超时就更新一下头像和昵称
            if (loginUser == null) {
                userMapper.updateByPrimaryKey(wxUser);
                result.setWxNickname(wxUser.getNickname());
                result.setAvatar(wxUser.getAvatar());
            }
        }
        result.setSuperAdmin(roleDao.isSuperAdmin(result.getOpenId()));
        result.setArenaAdmin(roleDao.isAreaAdmin(result.getOpenId()));
        result.setAccountant(roleDao.isAccountant(result.getOpenId()));

        //保存到cookie里
        coder.encode(result, resp);

        return result;
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
