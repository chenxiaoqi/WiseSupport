package com.lazyman.timetennis.wp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.TreeMap;

@Component
@Slf4j
public class TradeMonitor {

    private PayDao payDao;

    public TradeMonitor(PayDao payDao) {
        this.payDao = payDao;
    }

    String onNotify(TreeMap<String, String> tradeNotify) {
        String tradeState = tradeNotify.get("trade_state");
        String status;
        if (tradeState == null) {
            log.info("trade no trade state found. {}", tradeNotify);
            status = "wp";
        } else {
            String transactionId = tradeNotify.get("transaction_id");
            String tradNo = tradeNotify.get("out_trade_no");
            Trade trade = payDao.load(tradNo);
            if ("SUCCESS".equals(tradeState)) {
                int totalFee = Integer.parseInt(tradeNotify.get("total_fee"));
                if (!trade.getStatus().equals("wp")) {
                    log.info("trade {} already in status [{}],ignore", tradNo, trade.getStatus());
                    return trade.getStatus();
                }
                if (totalFee != trade.getFee()) {
                    status = "fnm";
                    log.error("trade {} fee not match expect {}, actual{}", tradNo, trade.getFee(), totalFee);
                } else {
                    status = "ok";
                }
            } else {
                if ("NOTPAY".equals(tradeState)) {
                    status = "wp";
                } else {
                    status = "fail";
                }
            }
            log.info("update trade {} status from {} to {}", tradNo, trade.getStatus(), status);
            payDao.updateStatus(tradNo, status, transactionId);
        }
        return status;
    }
}
