package com.lazyman.timetennis.core;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class SecurityUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurityUtils() {
    }

    public static String randomSeq(int length) {
        byte[] bytes = new byte[length / 2];
        RANDOM.nextBytes(bytes);
        return Hex.encodeHexString(bytes, false);
    }

    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

    }


}
