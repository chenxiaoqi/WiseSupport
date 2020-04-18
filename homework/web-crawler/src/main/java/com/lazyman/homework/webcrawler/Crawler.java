package com.lazyman.homework.webcrawler;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class Crawler {

    private JdbcTemplate jdbcTemplate;
    private HttpClient httpClient;

    public Crawler(JdbcTemplate jdbcTemplate, HttpClient httpClient) {

        this.jdbcTemplate = jdbcTemplate;
        this.httpClient = httpClient;
    }

    @Scheduled(fixedDelay = 3000)
    public void run() throws IOException {

        HttpGet get = new HttpGet("https://www.baidu.com");
        httpClient.execute(get, new ResponseHandler<Void>() {
            @Override
            public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                return null;
            }
        });
    }

}
