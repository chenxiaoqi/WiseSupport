package com.lazyman.timetennis.wp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class TradeMonitor {

    private WePayService pay;

    private PayDao payDao;

    public TradeMonitor(WePayService pay, PayDao payDao) {
        this.pay = pay;
        this.payDao = payDao;
    }

    public String onNotify(Map<String, String> tradeNotify) {
        String tradNo = tradeNotify.get("out_trade_no");
        String transactionId = tradeNotify.get("transaction_id");
        int totalFee = Integer.parseInt(tradeNotify.get("total_fee"));

        Trade trade = payDao.load(tradNo);
        if (!trade.getStatus().equals("wp")) {
            log.info("trade {} already in status [{}],ignore", tradNo, trade.getStatus());
            return trade.getStatus();
        }

        String status;
        if ("SUCCESS".equals(tradeNotify.get("result_code"))) {
            if (totalFee != trade.getFee()) {
                status = "fm";
                log.error("trade {} fee not match expect {}, actual{}", tradNo, trade.getFee(), totalFee);
            } else {
                status = "ok";
            }
        } else {
            status = "fail";
        }
        payDao.updateStatus(tradNo, status, transactionId);
        //todo 支付失败的要取消场地预定
        return status;
    }
}
