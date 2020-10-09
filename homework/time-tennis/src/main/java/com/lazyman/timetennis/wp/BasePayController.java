package com.lazyman.timetennis.wp;

import com.lazyman.timetennis.core.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;

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

    protected Map<String, String> preparePay(String tradeNo, @Nullable String mchId, String openId, String productType, int totalFee, String desc, PrepareCallback callback) {

        if (StringUtils.isEmpty(mchId)) {
            mchId = this.platformMchId;
        }
        String prepayId = pay.prepay(mchId, openId, tradeNo, String.valueOf(totalFee), desc);
        payDao.createTrade(tradeNo, openId, productType, prepayId, totalFee, mchId);
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
