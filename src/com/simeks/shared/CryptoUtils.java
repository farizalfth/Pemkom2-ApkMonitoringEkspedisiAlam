package com.simeks.shared;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;

public class CryptoUtils {
    private static final String ALGORITHM = "AES";
    private static final byte[] KEY_BYTES = "MySuperSecretKey".getBytes();

    private static SecretKey getKey() { return new SecretKeySpec(KEY_BYTES, ALGORITHM); }

    public static SealedObject encrypt(Serializable object) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        return new SealedObject(object, cipher);
    }
    public static Object decrypt(SealedObject sealedObject) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getKey());
        return sealedObject.getObject(cipher);
    }
}