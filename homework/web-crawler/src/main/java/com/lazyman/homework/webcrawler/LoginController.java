package com.lazyman.homework.webcrawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class LoginController {

    private HttpClient client;

    public LoginController(HttpClient client) {
        this.client = client;
    }

    @GetMapping("/login")
    public String login(String code, Model model) throws IOException {
        client.execute(RequestBuilder.get("https://app.singlewindow.cn/cas/login?service=http%3A%2F%2Fwww.singlewindow.cn%2Fsinglewindow%2Flogin.jspx&logoutFlag=1&_swCardF=1").build(), new ResponseHandler<Void>() {
            @Override
            public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                Document document = Jsoup.parse(response.getEntity().getContent(), StandardCharsets.UTF_8.name(), "https://app.singlewindow.cn");
                model.addAttribute("random", document.select("#random").first().attr("value"));
                model.addAttribute("serverDate", document.select("#serverDate").first().attr("value"));
                model.addAttribute("lt", document.select("#lt").first().attr("value"));
                model.addAttribute("execution", document.select("#execution").first().attr("value"));
                return null;
            }
        });
        return "dummy.html";
    }

    @PostMapping(value = "/login", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JSONArray login(String icCard, String certNo, String signData, String random, String serverDate, String userPin, String lt, String execution, String lpid, String swLoginFlag, String _eventId, Model model) throws IOException {
        HttpUriRequest req = RequestBuilder.post("https://app.singlewindow.cn/cas/login?service=https%3A%2F%2Fswapp.singlewindow.cn%2Fdeskserver%2Fj_spring_cas_security_check&logoutFlag=1&_swCardF=1")
                .addParameter("icCard", icCard)
                .addParameter("certNo", certNo)
                .addParameter("signData", signData)
                .addParameter("random", random)
                .addParameter("serverDate", serverDate)
                .addParameter("userPin", userPin)
                .addParameter("lt", lt)
                .addParameter("execution", execution)
                .addParameter("lpid", lpid)
                .addParameter("swLoginFlag", swLoginFlag)
                .addParameter("_eventId", _eventId).build();

        client.execute(req, (ResponseHandler<Void>) response -> null);

        client.execute(new HttpGet("https://swapp.singlewindow.cn/deskserver/sw/deskIndex?menu_id=spl"), (ResponseHandler<Void>) response -> null);

//        String location = client.execute(new HttpGet("https://app.singlewindow.cn/cas/login?service=https%3A%2F%2Fswapp.singlewindow.cn%2Fdeskserver%2Fj_spring_cas_security_check"), response -> {
//            return response.getFirstHeader(HttpHeaders.LOCATION).getValue();
//        });

//        client.execute(new HttpGet(location), response -> null);

        String rdtime = client.execute(new HttpPost("https://swapp.singlewindow.cn/splserver/sw/spl/para/getUserinfo"), new ResponseHandler<String>() {
            @Override
            public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                JSONObject object = JSON.parseObject(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
                return (String) JSONPath.eval(object, "$.unSafeBusinessData.data.rdtime");
            }
        });

        req = RequestBuilder.get("https://swapp.singlewindow.cn/splserver/spl/epiTaxOptimize/queryCusEpiTaxRecodes?limit=10&offset=0&fromCondition=%7B%22entryId%22%3A%22%22%2C%22taxId%22%3A%22%22%2C%22billNo%22%3A%22%22%2C%22contrNo%22%3A%22%22%2C%22ownerName%22%3A%22%22%2C%22taxType%22%3A%22%22%2C%22declPort%22%3A%22%22%2C%22extendField1%22%3A%22%22%2C%22genDateStart%22%3A%222020-04-15%22%2C%22genDateEnd%22%3A%222020-04-30%22%2C%22transStatus%22%3A%22N%22%7D&_=1588126631281")
                .addHeader("rdtime", rdtime).build();
        return client.execute(req, response -> {
            String jsonString = (String) JSON.parse(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            return (JSONArray) JSONPath.eval(JSON.parseObject(jsonString), "$.unSafeBusinessData.rows");
        });

    }

}
