package com.lazyman.timetennis.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.SessionWatch;
import com.lazyman.timetennis.privilege.RoleDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@Slf4j
@Validated
public class LoginController {

    private HttpClient client;

    private String appId;

    private String secret;

    private UserMapper userMapper;

    private RoleDao roleDao;

    public LoginController(HttpClient client,
                           @Value("${wx.app-id}") String appId,
                           @Value("${wx.secret}") String secret,
                           UserMapper userMapper, RoleDao roleDao) {
        this.client = client;
        this.appId = appId;
        this.secret = secret;
        this.userMapper = userMapper;
        this.roleDao = roleDao;
    }

    @GetMapping("/login")
    public User login(@NotEmpty String jsCode, @NotEmpty String rawData, @NotEmpty String signature, HttpServletRequest request) throws IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(Constant.SK_USER);
            if (user != null) {
                log.debug("find user in session return directly");
                return user;
            }
        }
        User user = new User();
        HttpUriRequest get = RequestBuilder.get("https://api.weixin.qq.com/sns/jscode2session")
                .addParameter("appid", appId)
                .addParameter("secret", secret)
                .addParameter("js_code", jsCode)
                .addParameter("grant_type", "authorization_code")
                .build();
        String sessionKey = client.execute(get, response -> {
            JSONObject json = JSON.parseObject(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            String openId = json.getString("openid");
            Assert.notNull(openId, () -> "WeChat jscode2session failed : " + json);
            user.setOpenId(openId);
            return json.getString("session_key");
        });
        String expect = Hex.encodeHexString(DigestUtils.sha1(rawData + sessionKey));
        Assert.isTrue(expect.equals(signature), () -> "verify signature failed expect " + expect + " actual " + signature);
        JSONObject json = JSON.parseObject(rawData);
        user.setWxNickname(json.getString("nickName"));
        user.setAvatar(json.getString("avatarUrl"));

        User dbUser = userMapper.selectByPrimaryKey(user.getOpenId());
        if (dbUser == null) {
            userMapper.insert(user);
        } else {
            userMapper.updateByPrimaryKey(user);
        }
        session = request.getSession();
        SessionWatch.register(user.getOpenId(), session);

        dbUser = userMapper.selectByPrimaryKey(user.getOpenId());
        dbUser.setSuperAdmin(roleDao.isSuperAdmin(user.getOpenId()));
        dbUser.setArenaAdmin(roleDao.isAreaAdmin(user.getOpenId()));

        //todo 兼容老版本
        dbUser.setAccountant(roleDao.isAccountant(user.getOpenId()));
        session.setAttribute("user", dbUser);
        return dbUser;
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
