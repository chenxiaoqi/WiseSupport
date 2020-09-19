package com.lazyman.timetennis.core;

import org.apache.commons.codec.binary.Hex;

import java.security.SecureRandom;

public final class SecurityUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurityUtils() {
    }

    public static final String randomSeq(int length) {
        byte[] bytes = new byte[length / 2];
        RANDOM.nextBytes(bytes);
        return Hex.encodeHexString(bytes, false);
    }

}
