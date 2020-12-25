package com.lazyman.timetennis.task;

import com.lazyman.timetennis.wp.PayDao;
import com.lazyman.timetennis.wp.Trade;
import com.lazyman.timetennis.wp.TradeEvent;
import com.lazyman.timetennis.wp.WePayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShareTask implements ApplicationListener<TradeEvent> {

    private final PayDao payDao;

    private final WePayService pay;

    private final int shareRate;

    public ShareTask(PayDao payDao, WePayService pay, @Value("${wx.pay-share-rate}") int shareRate) {
        this.payDao = payDao;
        this.pay = pay;
        this.shareRate = shareRate;
    }

    @Scheduled(fixedRate = 120000)
    public void run() {
        long start = System.currentTimeMillis();
        int count = 0;
        Trade trade;
        while ((trade = payDao.pollWaitForShare()) != null) {
            int total = trade.getFee();
            int serviceFee = total * 6 / 1000;
            boolean success = pay.sharePayment(trade.getTradeNo(), trade.getTransactionId(), trade.getReceiverId(), trade.getReceiverType(), (total - serviceFee) * this.shareRate / 1000);
            if (success) {
                payDao.updateShareStatus(trade.getTradeNo(), "ok");
            } else {
                payDao.updateShareStatus(trade.getTradeNo(), "fail");
            }
            count++;
        }
        log.info("share task process {} records in {}s", count, (System.currentTimeMillis() - start) / 1000);
    }

    @Override
    public void onApplicationEvent(TradeEvent event) {
        Trade trade = event.getTrade();

        if (!trade.getStatus().equals("ok") || trade.getReceiverId() == null) {
            return;
        }

        if (trade.getShareStatus() == null) {
            payDao.updateShareStatus(trade.getTradeNo(), "wfs");
        }
    }
}
