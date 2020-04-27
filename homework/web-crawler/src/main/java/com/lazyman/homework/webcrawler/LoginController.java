package com.lazyman.homework.webcrawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class LoginController {

    private HttpClient client;

    public LoginController(HttpClient client) {
        this.client = client;
    }

    @GetMapping("/login")
    public JSONObject login(String code) throws IOException {

        String connectionUrl =
                "jdbc:sqlserver://yourserver.database.windows.net:1433;database=AdventureWorks;encrypt=true;trustServerCertificate=false;loginTimeout=30;";

        HttpGet req = new HttpGet("https://api.weixin.qq.com/sns/jscode2session?appid=wxa87c26db862e6bbd&secret=ace15be8afc9c3072d94c217707fae17&js_code=" + code + "&grant_type=authorization_code");
        JSONObject object = client.execute(req, response -> JSON.parseObject(response.getEntity().getContent(), StandardCharsets.UTF_8, JSONObject.class));
        System.out.println(object);
        return object;
    }

}
