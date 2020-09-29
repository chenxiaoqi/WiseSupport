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

    protected Map<String, String> preparePay(String tradeNo, String openId, String productType, int totalFee, String desc, PrepareCallback callback) {

        //todo 商户ID要用场地对应商户ID，而不是平台的商户ID
        String prepayId = pay.prepay(this.platformMchId, openId, tradeNo, String.valueOf(totalFee), desc);
        payDao.createTrade(tradeNo, openId, productType, prepayId, totalFee, platformMchId);
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
