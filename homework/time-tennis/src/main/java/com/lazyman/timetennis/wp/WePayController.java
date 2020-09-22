package com.lazyman.timetennis.wp;

import com.lazyman.timetennis.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.lang.NonNull;
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
@RequestMapping("/pay")
public class WePayController implements ApplicationContextAware {
    private WePayService pay;

    private TradeService tradeService;

    private PayDao payDao;

    private ApplicationContext context;

    public WePayController(WePayService pay, TradeService tradeService, PayDao payDao) {
        this.pay = pay;
        this.tradeService = tradeService;
        this.payDao = payDao;
    }

    @PostMapping("/notify")
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

        String tradeNo = params.get("out_trade_no");
        try {
            Trade trade = payDao.load(tradeNo);
            if (params.get("result_code").equals("SUCCESS")) {
                params.put("trade_state", "SUCCESS");
            } else {
                params.put("trade_state", "notify-fail");
            }
            String status = tradeService.onNotify(trade, params);
            trade.setStatus(status);
            context.publishEvent(new TradeEvent(this, trade));
        } catch (EmptyResultDataAccessException e) {
            log.error("out_trade_no {} does not exists.", tradeNo);
        }
        Map<String, String> result = new HashMap<>();
        result.put("return_code", "SUCCESS");
        result.put("return_msg", "OK");
        response.getWriter().write(pay.generateXml(result));
    }

    @GetMapping("/trade_status")
    @Transactional
    @ResponseBody
    public String queryTradeStatus(@SessionAttribute User user, @RequestParam String tradeNo) {
        Trade trade = payDao.load(tradeNo);
        Validate.isTrue(user.getOpenId().equals(trade.getOpenId()));
        String status;
        if (trade.getStatus().equals("wp")) {
            status = tradeService.onNotify(trade, pay.queryTrade(tradeNo, trade.getMchId()));
            trade.setStatus(status);
            //todo 是否要发送时间,删除预定
        } else {
            status = trade.getStatus();
        }
        return status;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
