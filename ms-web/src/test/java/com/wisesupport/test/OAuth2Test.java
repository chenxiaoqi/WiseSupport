package com.wisesupport.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Author chenxiaoqi on 2019-01-20.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OAuth2Test {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void token_client() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        String response = restTemplate.withBasicAuth("client", "123456").
                postForObject("/oauth/token", params, String.class);
        System.out.println(response);
    }
}
