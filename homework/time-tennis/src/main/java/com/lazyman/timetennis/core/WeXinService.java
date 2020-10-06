package com.lazyman.timetennis.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class WeXinService {
    private String appId;

    private String secret;

    private HttpClient client;

    private String accessToken;

    public WeXinService(@Value("${wx.app-id}") String appId, @Value("${wx.secret}") String secret, HttpClient client) {
        this.appId = appId;
        this.secret = secret;
        this.client = client;
    }

    @PostConstruct
    public void init() throws IOException {
        HttpUriRequest request = RequestBuilder.get("https://api.weixin.qq.com/cgi-bin/token")
                .addParameter("grant_type", "client_credential")
                .addParameter("appid", this.appId)
                .addParameter("secret", this.secret).build();
        this.accessToken = client.execute(request, response -> {
            JSONObject jo = JSON.parseObject(response.getEntity().getContent(), JSONObject.class);
            String token = jo.getString("access_token");
            Validate.notEmpty(token, "get access toke failed. %s", jo);
            return token;
        });
    }

    public WeXinToken getWeXinToken(String jsCode) throws IOException {
        HttpUriRequest get = RequestBuilder.get("https://api.weixin.qq.com/sns/jscode2session")
                .addParameter("appid", appId)
                .addParameter("secret", secret)
                .addParameter("js_code", jsCode)
                .addParameter("grant_type", "authorization_code")
                .build();
        return client.execute(get, response -> {
            JSONObject json = JSON.parseObject(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            String openId = json.getString("openid");
            Assert.notNull(openId, () -> "WeChat jscode2session failed : " + json);
            return new WeXinToken(openId, json.getString("session_key"));
        });
    }

    public void sendMessage() throws IOException {
        JSONObject jo = new JSONObject();
        jo.put("touser", "oA3ve4t84q0Nssa4kt6nPgvmmej0");
        jo.put("template_id", "XfwSuk6NAwyuVuw6yCUGTefMZLtjWa8cvGCI4KdW_aA");

        JSONObject data = new JSONObject();

        JSONObject value = new JSONObject();
        value.put("value", "2020-02-02");
        data.put("time1", value);

        value = new JSONObject();
        value.put("value", "19:00");
        data.put("time2", value);

        value = new JSONObject();
        value.put("value", "3333");
        data.put("thing3", value);

        value = new JSONObject();
        value.put("value", "4444元");
        data.put("amount4", value);

        jo.put("data", data);

        HttpUriRequest post = RequestBuilder
                .post("https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + this.accessToken)
                .setEntity(new StringEntity(jo.toJSONString(), ContentType.APPLICATION_JSON)).build();

        client.execute(post, (ResponseHandler<Void>) response -> {
            if (log.isDebugEnabled()) {
                log.debug("send message {}", EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            }
            return null;
        });


    }


}
