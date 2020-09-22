package com.lazyman.timetennis.task;

import com.lazyman.timetennis.wp.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.TreeMap;

@Component
@Slf4j
public class TradeTask implements ApplicationContextAware {
    private PayDao payDao;

    private WePayService pay;

    private TradeService tradeService;

    private TransactionTemplate tt;

    private ApplicationContext context;

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
                        } else {
                            //任务只处理状态时 wp 的,这里直接设置成 end by task
                            pay.closeTrad(ft.getTradeNo(), ft.getMchId());
                            status = "ebt";
                            payDao.updateStatus(ft.getTradeNo(), status);
                        }
                        ft.setStatus(status);
                        context.publishEvent(new TradeEvent(this, ft));
                    }
                });
            } catch (Exception e) {
                log.error("deal trade {} failed.", ft.getTradeNo(), e);
            }
            count++;
        }
        log.info("trade task process {} records in {}s", count, (System.currentTimeMillis() - start) / 1000);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
