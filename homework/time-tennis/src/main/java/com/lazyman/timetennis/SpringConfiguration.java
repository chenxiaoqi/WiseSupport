package com.lazyman.timetennis;

import com.lazyman.timetennis.arena.ArenaDao;
import com.lazyman.timetennis.booking.BookingMapper;
import com.lazyman.timetennis.menbership.MembershipCardService;
import com.lazyman.timetennis.task.ChargeTask;
import com.wisesupport.commons.httpclient.RequestLoggerInterceptor;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.support.TransactionTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@Configuration
public class SpringConfiguration {

    @Bean(destroyMethod = "close")
    public CloseableHttpClient httpClient() throws NoSuchAlgorithmException, KeyManagementException {

        SSLContext context = SSLContextBuilder.create().build();
        return HttpClientBuilder.create()
                .setSSLContext(context)
                .setDefaultCookieStore(new BasicCookieStore())
                .addInterceptorLast(new RequestLoggerInterceptor())
                .build();
    }

    @Bean
    public ChargeTask dailyChargeTask(TaskScheduler scheduler, BookingMapper bookingMapper, ArenaDao arenaDao, MembershipCardService cardService, TransactionTemplate tt) {
        ChargeTask chargeTask = new ChargeTask(1, bookingMapper, arenaDao, cardService, tt);
        scheduler.schedule(chargeTask, new CronTrigger("0 0 1 * 1/1 ?"));
        return chargeTask;
    }

    @Bean
    public ChargeTask monthlyChargeTask(TaskScheduler scheduler, BookingMapper bookingMapper, ArenaDao arenaDao, MembershipCardService cardService, TransactionTemplate tt) {
        ChargeTask chargeTask = new ChargeTask(2, bookingMapper, arenaDao, cardService, tt);
        scheduler.schedule(chargeTask, new CronTrigger("0 0 1 1 1/1 ?"));
//        ChargeTask chargeTask = new ChargeTask(2, bookingMapper, arenaDao, cardService, tt);
//        chargeTask.setToday("2020-11-07");
        scheduler.scheduleAtFixedRate(chargeTask, Duration.ofMinutes(1));
        return chargeTask;
    }
}
