package com.lazyman.timetennis.task;

import com.lazyman.timetennis.wp.PayDao;
import com.lazyman.timetennis.wp.Trade;
import com.lazyman.timetennis.wp.TradeService;
import com.lazyman.timetennis.wp.WePayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.TreeMap;

@Component
@Slf4j
public class TradeTask {
    private PayDao payDao;

    private WePayService pay;

    private TradeService tradeService;

    private TransactionTemplate tt;

    public TradeTask(PayDao payDao, WePayService pay, TradeService tradeService, TransactionTemplate tt) {
        this.payDao = payDao;
        this.pay = pay;
        this.tradeService = tradeService;
        this.tt = tt;
    }


    @Scheduled(fixedRate = 120000)
    public void run() {
        long start = System.currentTimeMillis();
        int count = 0;
        Trade trade;
        while ((trade = payDao.pollWaitForPay()) != null) {
            final Trade ft = trade;
            try {
                TreeMap<String, String> wpTrade = pay.queryTrade(ft.getTradeNo(), ft.getMchId());
                tt.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus tt) {
                        String status = tradeService.onNotify(ft, wpTrade);
                        if (status.equals("ok")) {
                            //订单已经支付完成了,应该不可能
                            log.warn("trade {} complete in task", ft.getTradeNo());
                            return;
                        }
                        pay.closeTrad(ft.getTradeNo(), ft.getMchId());
                        payDao.updateStatus(ft.getTradeNo(), "ebt");
                        payDao.deleteTradeBooking(ft.getTradeNo());
                        log.info("delete user {} bookings trade {} in stats {} ", ft.getOpenId(), ft.getTradeNo(), ft.getStatus());
                    }
                });
            } catch (Exception e) {
                log.error("deal trade {} failed.", ft.getTradeNo(), e);
            }
            count++;
        }
        log.info("trade task process {} records in {}s", count, (System.currentTimeMillis() - start) / 1000);
    }

}
