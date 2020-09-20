package com.lazyman.timetennis.wp;

import com.lazyman.timetennis.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Controller
@Slf4j
public class WePayController {
    private WePayService pay;

    private TradeMonitor monitor;

    private PayDao payDao;

    public WePayController(WePayService pay, TradeMonitor monitor, PayDao payDao) {
        this.pay = pay;
        this.monitor = monitor;
        this.payDao = payDao;
    }

    @PostMapping("/pay/notify")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        TreeMap<String, String> params = pay.parse(request.getInputStream());
        log.debug("receive pay notify : {}", params);

        String sign = params.remove("sign");
        Validate.notEmpty(sign);
        String expect = pay.createSign(params);
        Validate.isTrue(sign.equals(expect), "invalid sign in pay notify");

        String returnCode = params.get("return_code");
        if (!"SUCCESS".equals(returnCode)) {
            return;
        }

        monitor.onNotify(params);

        Map<String, String> result = new HashMap<>();
        result.put("return_code", "SUCCESS");
        result.put("return_msg", "OK");
        response.getWriter().write(pay.generateXml(result));
    }

    @GetMapping("/pay/trade_status")
    @Transactional
    @ResponseBody
    public String queryTradeStatus(@SessionAttribute User user, @RequestParam String tradeNo) {
        Trade trade = payDao.load(tradeNo);
        Validate.isTrue(user.getOpenId().equals(trade.getOpenId()));
        String status;
        if (trade.getStatus().equals("wp")) {
            //失败删除场地预定？？
            return monitor.onNotify(pay.queryTrade(tradeNo, trade.getMchId()));
        } else {
            status = trade.getStatus();
        }
        return status;
    }
}
