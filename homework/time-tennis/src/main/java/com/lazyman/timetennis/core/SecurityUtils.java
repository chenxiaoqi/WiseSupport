package com.lazyman.timetennis.core;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public final class SecurityUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String AES_KEY_ALGORITHM = "AES";

    private static final String AES_ALGORITHM = "AES/CBC/PKCS7Padding";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private SecurityUtils() {
    }

    public static String randomSeq(int length) {
        byte[] bytes = new byte[length / 2];
        RANDOM.nextBytes(bytes);
        return Hex.encodeHexString(bytes, false);
    }

    public static byte[] aesDecrypt(byte[] value, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, AES_KEY_ALGORITHM), new IvParameterSpec(iv));
            return cipher.doFinal(value);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(String[] args) {

    }


}
