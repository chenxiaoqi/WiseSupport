package com.lazyman.timetennis.user;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.MessageDigest;

@Component
@Slf4j
public class UserCoder {
    private byte[] bytesKey;

    private UserCoder(@Value("${wx.app-secret}") String key) {
        this.bytesKey = key.getBytes();
    }

    public void encode(User user, HttpServletResponse response) {
        try {
            Cookie cookie = new Cookie("JSESSIONID", encode(user));
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public User decode(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token == null) {
            return null;
        }
        if (token.length() <= 48) {
            log.warn("invalid jsessionid length expect more than 48, {}", token);
            return null;
        }

        byte[] bytes;
        try {
            bytes = validate(token);
            User user = decode(bytes);
            if (user == null) {
                log.info("token expired.{}", token);
            }
            return user;
        } catch (DecoderException | IOException e) {
            log.warn("decode token failed {}", token, e);
            return null;
        }

    }

    private User decode(byte[] bytes) throws IOException {
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(bytes));
        String version = din.readUTF();

        long timestamp = din.readLong();
        if (timestamp + DateUtils.MILLIS_PER_HOUR * 24 < System.currentTimeMillis()) {
            return null;
        }
        User user = new User();
        user.setOpenId(din.readUTF());
        user.setArenaAdmin(din.readBoolean());
        user.setSuperAdmin(din.readBoolean());
        user.setVip(din.readBoolean());
        user.setAccountant(din.readBoolean());
        user.setBalance(din.readInt());
        return user;
    }

    private static byte[] toBytes(User user) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(256);
        DataOutputStream dout = new DataOutputStream(out);
        dout.writeUTF("v1");
        dout.writeLong(System.currentTimeMillis());

        dout.writeUTF(user.getOpenId());
        dout.writeBoolean(user.isArenaAdmin());
        dout.writeBoolean(user.isSuperAdmin());
        dout.writeBoolean(user.getVip());
        dout.writeBoolean(user.isAccountant());
        dout.writeInt(user.getBalance());
        return out.toByteArray();
    }

    private String encode(User user) throws IOException {
        byte[] bytes = toBytes(user);
        MessageDigest digest = DigestUtils.getMd5Digest();
        digest.update(bytes);
        byte[] sign = digest.digest(bytesKey);
        return Hex.encodeHexString(bytes) + Hex.encodeHexString(sign);
    }

    private byte[] validate(String token) throws DecoderException {
        byte[] bytes = Hex.decodeHex(token);
        MessageDigest digest = DigestUtils.getMd5Digest();
        digest.update(bytes, 0, bytes.length - 16);
        byte[] sign = digest.digest(bytesKey);
        boolean match = true;
        int bytesStart = bytes.length - 16;
        for (int i = 0; i < 16; i++) {
            if (sign[i] != bytes[bytesStart + i]) {
                match = false;
                break;
            }
        }
        if (!match) {
            throw new DecoderException("token sign mismatch");
        }
        return bytes;
    }

    public static void main(String[] args) throws IOException, DecoderException {
        UserCoder coder = new UserCoder("1111111");
        User user = new User();
        user.setOpenId("1111");
        user.setVip(true);
        user.setAdmin(false);
        user.setBalance(9999);
        String token = coder.encode(user);

        byte[] bytes = coder.validate(token);
        coder.decode(bytes);
        System.out.println(user.getOpenId());
        System.out.println(user.getVip());
        System.out.println(user.getAdmin());
        System.out.println(user.getBalance());
    }
}
