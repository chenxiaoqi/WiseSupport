package com.lazyman.timetennis.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import javax.servlet.http.HttpServletResponse;
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

    private UserCoder coder;

    public LoginController(HttpClient client,
                           @Value("${wx.app-id}") String appId,
                           @Value("${wx.secret}") String secret,
                           UserMapper userMapper, RoleDao roleDao, UserCoder coder) {
        this.client = client;
        this.appId = appId;
        this.secret = secret;
        this.userMapper = userMapper;
        this.roleDao = roleDao;
        this.coder = coder;
    }

    @GetMapping("/login")
    public User login(@NotEmpty String jsCode, @NotEmpty String rawData, @NotEmpty String signature, HttpServletRequest request, HttpServletResponse resp) throws IOException {

        User loginUser = coder.decode(request);
        User result;
        if (loginUser == null) {
            HttpUriRequest get = RequestBuilder.get("https://api.weixin.qq.com/sns/jscode2session")
                    .addParameter("appid", appId)
                    .addParameter("secret", secret)
                    .addParameter("js_code", jsCode)
                    .addParameter("grant_type", "authorization_code")
                    .build();
            User wxUser = client.execute(get, response -> {
                JSONObject json = JSON.parseObject(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
                String openId = json.getString("openid");
                Assert.notNull(openId, () -> "WeChat jscode2session failed : " + json);
                User user = new User();
                user.setOpenId(openId);
                String sessionKey = json.getString("session_key");
                String expect = Hex.encodeHexString(DigestUtils.sha1(rawData + sessionKey));
                Assert.isTrue(expect.equals(signature), () -> "verify signature failed expect " + expect + " actual " + signature);
                JSONObject raw = JSON.parseObject(rawData);
                user.setWxNickname(raw.getString("nickName"));
                user.setAvatar(raw.getString("avatarUrl"));
                return user;
            });
            result = userMapper.selectByPrimaryKey(wxUser.getOpenId());
            if (result == null) {
                userMapper.insert(wxUser);
            } else {
                userMapper.updateByPrimaryKey(wxUser);
            }
            result = userMapper.selectByPrimaryKey(wxUser.getOpenId());
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
