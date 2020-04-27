package com.lazyman.homework.webcrawler.porteasy;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.RequestBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class Crawler {

    public static final String URL = "http://www.porteasy.cn/cn/Declare/search.aspx";

    private static final String INSERT_SQL = "INSERT INTO [商编库]\n" +
            "           ([版本]\n" +
            "           ,[商品编码]\n" +
            "           ,[商品名称]\n" +
            "           ,[申报要素]\n" +
            "           ,[法定第一单位]\n" +
            "           ,[法定第二单位]\n" +
            "           ,[最惠国进口税率]\n" +
            "           ,[普通进口税率]\n" +
            "           ,[暂定进口税率]\n" +
            "           ,[消费税率]\n" +
            "           ,[出口关税率]\n" +
            "           ,[出口退税率]\n" +
            "           ,[增值税率]\n" +
            "           ,[海关监管条件]\n" +
            "           ,[商检监管条件]\n" +
            "           ,[3C认证]\n" +
            "           ,[能效标识]\n" +
            "           ,[备注])\n" +
            "     VALUES\n" +
            "           (?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?\n" +
            "           ,?)";
    private JdbcTemplate jdbcTemplate;

    private HttpClient httpClient;


    public Crawler(JdbcTemplate jdbcTemplate, HttpClient httpClient) {

        this.jdbcTemplate = jdbcTemplate;
        this.httpClient = httpClient;
    }

    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000)
    public void run() throws IOException, InterruptedException {

        ReqContext context = init();

        for (int i = 2; i <= context.getTotal(); i++) {
            System.out.printf("page %s\n", i);
            send(context, i);
            Thread.sleep(500);
        }

    }

    private void send(ReqContext context, int page) throws IOException {
        RequestBuilder builder = RequestBuilder.post(URL);

        context.getHiddenFields().put("__EVENTTARGET", "ddlPager");
        context.getHiddenFields().put("__EVENTARGUMENT", "");
        context.getHiddenFields().put("__LASTFOCUS", "");
        context.getHiddenFields().forEach(builder::addParameter);
        builder.addParameter("ddlPager", String.valueOf(page));

        addHeader(builder);

        httpClient.execute(builder.build(), (ResponseHandler<Void>) response -> {
            Element body = Jsoup.parse(response.getEntity().getContent(), StandardCharsets.UTF_8.name(), URL).body();
            extract(body, context);
            return null;
        });

    }

    private void addHeader(RequestBuilder builder) {
        builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0");
        builder.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        builder.addHeader("Accept-Language", "zh-CN,en-US;q=0.7,en;q=0.3");
        builder.addHeader("Referer", "http://www.porteasy.cn/cn/Declare/search.aspx");
    }

    private void extract(Element body, ReqContext context) {
        Elements dataListEls = body.select("table#ListView1_table table");
        for (Element dataEl : dataListEls) {
            Elements rowEls = dataEl.select("td.line5");
            Iterator<Element> itr = rowEls.iterator();

            List<String> args = new ArrayList<>();
            args.add(context.getVersion());
            while (itr.hasNext()) {
                String name = itr.next().text();
                String value = itr.next().text().trim();
                args.add(value);
            }
            jdbcTemplate.update(INSERT_SQL, args.toArray());
        }
    }

    private ReqContext init() throws IOException {
        RequestBuilder builder = RequestBuilder
                .get(URL);
        addHeader(builder);
        return httpClient.execute(builder.build(), response -> {
            Element body = Jsoup.parse(response.getEntity().getContent(), StandardCharsets.UTF_8.name(), URL).body();
            Elements elements = body.select("input[type=hidden]");
            ReqContext context = new ReqContext(body.select("#ddlPager>option").size());
            for (Element element : elements) {
                context.addHiddenFiled(element.attr("name"), element.attr("value"));
            }
            extract(body, context);
            return context;
        });
    }

}
