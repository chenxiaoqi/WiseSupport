package com.lazyman.timetennis.wp;

import com.lazyman.timetennis.core.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.TreeMap;

public class BasePayController {
    @Value("${wx.mch-id}")
    private String platformMchId;

    @Value("${wx.app-id}")
    private String appId;

    @Autowired
    protected WePayService pay;

    @Autowired
    protected PayDao payDao;

    protected Map<String, String> preparePay(String tradeNo, String openId, String productType, String receiverId, Integer receiveType, int totalFee, String desc, PrepareCallback callback) {
        boolean needShare = receiverId != null && !receiverId.equals(this.platformMchId);
        String prepayId = pay.prepay(openId, tradeNo, String.valueOf(totalFee), desc, needShare);
        if (needShare) {
            payDao.createTrade(tradeNo, openId, productType, prepayId, totalFee, receiverId, receiveType);
        } else {
            payDao.createTrade(tradeNo, openId, productType, prepayId, totalFee);
        }
        callback.call();

        TreeMap<String, String> params = new TreeMap<>();
        params.put("appId", appId);
        params.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("nonceStr", SecurityUtils.randomSeq(32));
        params.put("package", "prepay_id=" + prepayId);
        params.put("signType", "MD5");

        params.put("paySign", pay.createSign(params));

        //返回tradeNo让前台拿可以查询
        params.put("tradeNo", tradeNo);
        return params;
    }

    protected interface PrepareCallback {
        void call();
    }
}
