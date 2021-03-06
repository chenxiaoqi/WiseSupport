package com.lazyman.timetennis.wp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.core.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service
@Slf4j
public class WePayService {
    private static final String PAY_PREFIX = "https://api.mch.weixin.qq.com";

    private static final FastDateFormat FORMAT = FastDateFormat.getInstance("yyyyMMddhhmmssSSS");

    private static String previewTradeNo;

    private final static DocumentBuilder documentBuilder;

    private final static TransformerFactory transformerFactory;

    private final HmacUtils hmacUtils;

    static {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            transformerFactory = TransformerFactory.newInstance();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    private final String appId;

    private final String signKey;

    private String sandboxSignKey;

    private final boolean useSandbox;

    private final String notifyUrl;

    private final String spBillCreateIp;

    private final String platformMchId;

    private final int tradeExpireMinutes;

    private final HttpClient httpClient;

    public WePayService(@Value("${wx.app-id}") String appId,
                        @Value("${wx.pay-sign-key}") String signKey,
                        @Value("${wx.pay-use-sandbox}") boolean useSandbox,
                        @Value("${wx.pay-notify-url}") String notifyUrl,
                        @Value("${wx.pay-sp-bill-create-ip}") String spBillCreateIp,
                        @Value("${wx.mch-id}") String platformMchId,
                        @Value("${wx.pay-expire-minutes}") int tradeExpireMinutes,
                        HttpClient httpClient) {
        this.appId = appId;
        this.signKey = signKey;
        this.useSandbox = useSandbox;
        this.notifyUrl = notifyUrl;
        this.spBillCreateIp = spBillCreateIp;
        this.platformMchId = platformMchId;
        this.tradeExpireMinutes = tradeExpireMinutes;
        this.httpClient = httpClient;
        this.hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, signKey);
    }

    @PostConstruct
    public void init() throws IOException {
        if (this.useSandbox) {
            this.sandboxSignKey = getSignKey();
        }
    }

    void addShareReceiver() {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", this.platformMchId);
        params.put("nonce_str", SecurityUtils.randomSeq(32));
        params.put("sign_type", "HMAC-SHA256");

        JSONObject jo = new JSONObject();
        jo.put("type", "PERSONAL_OPENID");
        jo.put("account", "oA3ve4t84q0Nssa4kt6nPgvmmej0");
        jo.put("name", "陈小奇");
        jo.put("relation_type", "STORE_OWNER");
        params.put("receiver", jo.toJSONString());

        try {
            sendRequest(params, "/pay/profitsharingaddreceiver");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean sharePayment(String tradNo, String transactionId, String receiverId, Integer receiverType, int amount) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", this.platformMchId);
        params.put("nonce_str", SecurityUtils.randomSeq(32));
        params.put("sign_type", "HMAC-SHA256");
        params.put("transaction_id", transactionId);
        params.put("out_order_no", tradNo);

        JSONArray ja = new JSONArray();
        JSONObject jo = new JSONObject();
        jo.put("type", receiverType == 1 ? "PERSONAL_OPENID" : "MERCHANT_ID");
        jo.put("account", receiverId);
        jo.put("amount", amount);
        jo.put("description", "分账到商户");

        ja.add(jo);
        params.put("receivers", ja.toJSONString());

        try {
            Map<String, String> result = sendRequest(params, "/secapi/pay/profitsharing");
            return Objects.equals(result.get("result_code"), "SUCCESS");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    String prepay(String openId, String tradNo, String fee, String desc, boolean needShare) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", this.platformMchId);
        params.put("nonce_str", SecurityUtils.randomSeq(32));
        params.put("sign_type", "MD5");
        params.put("body", desc);
        params.put("out_trade_no", tradNo);
        params.put("total_fee", fee);
        params.put("fee_type", "CNY");
        params.put("spbill_create_ip", spBillCreateIp);
        Date now = new Date();
        params.put("time_start", Constant.FORMAT_COMPACT.format(now));
        params.put("time_expire", Constant.FORMAT_COMPACT.format(DateUtils.addMinutes(now, tradeExpireMinutes)));
        params.put("notify_url", notifyUrl);
        params.put("trade_type", "JSAPI");
        params.put("openid", openId);
        if (needShare) {
            params.put("profit_sharing", "Y");
        }
        try {
            Map<String, String> result = sendRequest(params, "/pay/unifiedorder");
            return result.get("prepay_id");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public TreeMap<String, String> queryTrade(String tradNo) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", this.platformMchId);
        params.put("out_trade_no", tradNo);
        params.put("nonce_str", SecurityUtils.randomSeq(32));
        params.put("sign_type", "MD5");
        try {
            return sendRequest(params, "/pay/orderquery");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void closeTrad(String tradeNo) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", this.platformMchId);
        params.put("out_trade_no", tradeNo);
        params.put("nonce_str", SecurityUtils.randomSeq(32));
        params.put("sign_type", "MD5");
        try {
            TreeMap<String, String> result = sendRequest(params, "/pay/closeorder");
            String code = result.get("result_code");
            if (!"SUCCESS".equals(code)) {
                code = result.get("err_code");
                if ("ORDERPAID".equals(code)) {
                    log.warn("wired in close a trade {} already paid", tradeNo);
                    throw new IllegalStateException("close a already paid trade");
                } else if ("ORDERCLOSED".equals(code)) {
                    log.warn("trade already closed {}", tradeNo);
                } else {
                    log.error("close trade {} failed in status {}", tradeNo, code);
                    throw new IllegalStateException("close trade fail status " + code);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getSignKey() throws IOException {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("mch_id", platformMchId);
        params.put("nonce_str", SecurityUtils.randomSeq(32));
        params.put("sign_type", "MD5");
        Map<String, String> result = sendRequest(params, "/pay/getsignkey");
        return Objects.requireNonNull(result.get("sandbox_signkey"));
    }

    private TreeMap<String, String> sendRequest(TreeMap<String, String> params, String url) throws IOException {
        params.put("sign", createSign(params));
        log.debug("we pay request params {} => {}", url, params);

        HttpUriRequest post = RequestBuilder.post(useSandbox ? PAY_PREFIX + "/sandboxnew" + url : PAY_PREFIX + url)
                .setEntity(new StringEntity(generateXml(params), StandardCharsets.UTF_8))
                .build();
        return httpClient.execute(post, response -> {
            TreeMap<String, String> result = parse(response.getEntity().getContent());
            String returnCode = result.get("return_code");
            if (!"SUCCESS".equals(returnCode)) {
                log.error("pay failed {} => {}", url, result);
                throw new IllegalStateException("WeChat Pay Failed.");
            } else {
                log.debug("we pay result {} => {}", url, result);
            }
            return result;
        });
    }

    String createSign(TreeMap<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (StringUtils.isEmpty(entry.getValue())) {
                continue;
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
        }
        //getsignkey接口签名时没有sandboxSignKey,需要用商户key
        builder.append("key=").append(this.useSandbox ? (sandboxSignKey == null ? signKey : sandboxSignKey) : signKey);
        String signType = params.get("sign_type");
        if ("HMAC-SHA256".equals(signType)) {
            return Hex.encodeHexString(hmacUtils.hmac(builder.toString()), false);
        } else {
            return Hex.encodeHexString(DigestUtils.md5(builder.toString()), false);
        }
    }

    public synchronized static String creatTradeNo(String productType) {
        String tradNo;
        do {
            tradNo = FORMAT.format(new Date()) + productType;
            if (tradNo.equals(previewTradeNo)) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    //do nothing
                }
            } else {
                previewTradeNo = tradNo;
                break;
            }
        } while (true);
        return tradNo;
    }

    String generateXml(Map<String, String> params) {
        Document document = documentBuilder.newDocument();
        Element xml = document.createElement("xml");
        document.appendChild(xml);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            Element e = document.createElement(entry.getKey());
            e.setTextContent(entry.getValue());
            xml.appendChild(e);
        }

        try {
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    TreeMap<String, String> parse(InputStream in) {
        try {
            TreeMap<String, String> result = new TreeMap<>();
            Document doc = documentBuilder.parse(in);
            NodeList nodes = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    result.put(((Element) node).getTagName(), node.getTextContent());
                }
            }
            return result;
        } catch (SAXException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
