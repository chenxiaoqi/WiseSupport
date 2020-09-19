package com.lazyman.timetennis.wp;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.core.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
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

    private final static DocumentBuilder documentBuilder;

    private final static TransformerFactory transformerFactory;

    static {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            transformerFactory = TransformerFactory.newInstance();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    private String appId;

    private String signKey;

    private String sandboxSignKey;

    private boolean useSandbox;

    private String notifyUrl;

    private String spBillCreateIp;

    private String platformMchId;

    private TradeMonitor monitor;

    private HttpClient httpClient;

    public WePayService(@Value("${wx.app-id}") String appId,
                        @Value("${wx.pay-sign-key}") String signKey,
                        @Value("${wx.pay-use-sandbox}") boolean useSandbox,
                        @Value("${wx.pay-notify-url}") String notifyUrl,
                        @Value("${wx.pay-sp-bill-create-ip}") String spBillCreateIp,
                        @Value("${wx.mch-id}") String platformMchId, HttpClient httpClient) {
        this.appId = appId;
        this.signKey = signKey;
        this.useSandbox = useSandbox;
        this.notifyUrl = notifyUrl;
        this.spBillCreateIp = spBillCreateIp;
        this.platformMchId = platformMchId;
        this.httpClient = httpClient;
    }

    @PostConstruct
    public void init() throws TransformerException, IOException {
        if (this.useSandbox) {
            this.sandboxSignKey = getSignKey();
        }
    }

    public String prepay(String mchId, String openId, String tradNo, String fee, String desc) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", SecurityUtils.randomSeq(32));
        params.put("sign_type", "MD5");
        params.put("body", desc);
        params.put("out_trade_no", tradNo);
        params.put("total_fee", fee);
        params.put("fee_type", "CNY");
        params.put("spbill_create_ip", spBillCreateIp);
        Date now = new Date();
        params.put("time_start", Constant.FORMAT_COMPACT.format(now));
        params.put("time_expire", Constant.FORMAT_COMPACT.format(DateUtils.addMinutes(now, 10)));
        params.put("notify_url", notifyUrl);
        params.put("trade_type", "JSAPI");
        params.put("openid", openId);
        try {
            Map<String, String> result = sendRequest(params, "/pay/unifiedorder");
            return result.get("prepay_id");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String queryTrade(String tradNo, String mchId) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("out_trade_no", tradNo);
        params.put("nonce_str", SecurityUtils.randomSeq(32));
        params.put("sign_type", "MD5");
        try {
            Map<String, String> result = sendRequest(params, "/pay/orderquery");
            return monitor.onNotify(result);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getSignKey() throws TransformerException, IOException {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("mch_id", platformMchId);
        params.put("nonce_str", SecurityUtils.randomSeq(32));
        Map<String, String> result = sendRequest(params, "/pay/getsignkey");
        return Objects.requireNonNull(result.get("sandbox_signkey"));
    }

    private Map<String, String> sendRequest(TreeMap<String, String> params, String url) throws IOException {
        params.put("sign", creatSign(params));
        log.debug("we pay request params {}", params);

        HttpUriRequest post = RequestBuilder.post(useSandbox ? PAY_PREFIX + "/sandboxnew" + url : PAY_PREFIX + url)
                .setEntity(new StringEntity(generateXml(params), StandardCharsets.UTF_8))
                .build();
        return httpClient.execute(post, response -> {
            Map<String, String> result = parse(response.getEntity().getContent());
            String returnCode = result.get("return_code");
            if (!"SUCCESS".equals(returnCode)) {
                log.error("pay failed {}", result);
                throw new IllegalStateException(result.get("return_msg"));
            } else {
                log.debug("we pay result {}", result);
            }
            return result;
        });
    }

    public String creatSign(TreeMap<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
        }
        builder.append("key=").append(this.useSandbox ? sandboxSignKey : signKey);
        return Hex.encodeHexString(DigestUtils.md5(builder.toString()), false);
    }

    public String creatTradeNo(String productType) {
        return FORMAT.format(new Date()) + productType;
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
