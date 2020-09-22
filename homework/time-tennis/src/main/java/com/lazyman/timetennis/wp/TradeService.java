package com.lazyman.timetennis.wp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.TreeMap;

@Component
@Slf4j
public class TradeService {

    private PayDao payDao;

    public TradeService(PayDao payDao) {
        this.payDao = payDao;
    }

    public String onNotify(Trade trade, TreeMap<String, String> tradeNotify) {
        String tradeState = tradeNotify.get("trade_state");
        String tradNo = tradeNotify.get("out_trade_no");
        String status;
        String transactionId = tradeNotify.get("transaction_id");
        if ("SUCCESS".equals(tradeState)) {
            int totalFee = Integer.parseInt(tradeNotify.get("total_fee"));
            if (trade.getStatus().equals("wp")) {
                //待支付状态
                status = "ok";
            } else if (trade.getStatus().equals("ok")) {
                //订单已经完成可能是重复调用,或者是查询订单状态过来的
                status = "ok";
            } else {
                //需要退钱了
                status = "rfud";
                log.error("need refund trade {} {}", tradNo, trade.getStatus());
            }
            if (totalFee != trade.getFee()) {
                status = "fnm";
                log.error("trade {} fee not match expect {}, actual{}", tradNo, trade.getFee(), totalFee);
            }
        } else {
            if ("NOTPAY".equals(tradeState)) {
                status = "wp";
            } else {
                status = "fail";
            }
        }
        if (!status.equals(trade.getStatus())) {
            log.info("update trade {}-{}, status from {} to {}", tradNo, tradeState, trade.getStatus(), status);
            payDao.updateStatus(tradNo, status, transactionId);
        }
        return status;
    }
}
