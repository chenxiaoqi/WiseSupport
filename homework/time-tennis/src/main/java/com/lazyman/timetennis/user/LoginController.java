package com.lazyman.timetennis.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lazyman.timetennis.core.WeXinService;
import com.lazyman.timetennis.core.WeXinToken;
import com.lazyman.timetennis.privilege.RoleDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
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

    private HttpClient client;

    private String appId;

    private String secret;

    private UserMapper userMapper;

    private RoleDao roleDao;

    private UserCoder coder;

    private WeXinService weXinService;

    public LoginController(HttpClient client,
                           @Value("${wx.app-id}") String appId,
                           @Value("${wx.secret}") String secret,
                           UserMapper userMapper,
                           RoleDao roleDao,
                           UserCoder coder,
                           WeXinService weXinService) {
        this.client = client;
        this.appId = appId;
        this.secret = secret;
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

        User loginUser = coder.decode(request);
        User result;
        if (loginUser == null) {
            WeXinToken token = weXinService.getWeXinToken(jsCode);
            String expect = Hex.encodeHexString(DigestUtils.sha1(rawData + token.getSessionKey()));
            Assert.isTrue(expect.equals(signature), () -> "verify signature failed expect " + expect + " actual " + signature);

            User user = new User();
            user.setOpenId(token.getOpenId());

            JSONObject raw = JSON.parseObject(rawData);
            user.setWxNickname(raw.getString("nickName"));
            user.setAvatar(raw.getString("avatarUrl"));

            result = userMapper.selectByPrimaryKey(user.getOpenId());
            if (result == null) {
                userMapper.insert(user);
            } else {
                userMapper.updateByPrimaryKey(user);
            }
            result = userMapper.selectByPrimaryKey(user.getOpenId());
        } else {
            result = userMapper.selectByPrimaryKey(loginUser.getOpenId());
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
