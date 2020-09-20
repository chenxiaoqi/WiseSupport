package com.lazyman.timetennis.wp;

import com.lazyman.timetennis.BusinessException;
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
        if (tradeState == null) {
            log.error("trade no trade state found. {}", tradeNotify);
            throw new BusinessException("no trade stat found");
        }

        String status;
        String transactionId = tradeNotify.get("transaction_id");
        String tradNo = tradeNotify.get("out_trade_no");
        Trade trade = payDao.load(tradNo);
        if (!"SUCCESS".equals(tradeState)) {
            status = "fail";
        } else {
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
        }
        log.info("update trade {} status from {} to {}", tradNo, trade.getStatus(), status);
        payDao.updateStatus(tradNo, status, transactionId);
        return status;
    }
}
