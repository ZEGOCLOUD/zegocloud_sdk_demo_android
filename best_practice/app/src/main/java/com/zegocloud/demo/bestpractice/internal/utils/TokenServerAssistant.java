package com.zegocloud.demo.bestpractice.internal.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONException;
import org.json.JSONObject;

public class TokenServerAssistant {
    static final private String VERSION_FLAG = "04";
    static final private int IV_LENGTH = 16;
    static final private String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    static public boolean VERBOSE = false;

    static public class Privileges {
        public boolean canLoginRoom;

        public boolean canPublishStream;

        public Privileges() {
            canLoginRoom = false;
            canPublishStream = false;
        }
    }

    public enum ErrorCode {
        SUCCESS(0),
        ILLEGAL_APP_ID(1),
        ILLEGAL_ROOM_ID(2),
        ILLEGAL_USER_ID(3),
        ILLEGAL_PRIVILEGE(4),
        ILLEGAL_SECRET(5),
        ILLEGAL_EFFECTIVE_TIME(6),
        OTHER(-1);

        ErrorCode(int code) {
            this.value = code;
        }

        public int value;
    }

    static public class ErrorInfo {
        public ErrorCode code;
        public String message;

        ErrorInfo() {
            code = ErrorCode.SUCCESS;
            message = "";
        }

        @Override
        public String toString() {
            return "{\"code\": " + code.value + ", \"message\": \"" + message + "\"}";
        }
    }

    static public class TokenInfo {
        public String data = "";

        public ErrorInfo error;

        TokenInfo() {
            this.error = new ErrorInfo();
        }

        @Override
        public String toString() {
            return "TokenInfo {\"error\": " + error + ", \"data\": \"" + data + "\"}";
        }
    }

    private TokenServerAssistant() {
    }

    /**
     * only for local test.You should get token from your server
     * @param appId
     * @param userId
     * @param secret
     * @param effectiveTimeInSeconds
     * @return
     * @throws JSONException
     */
    static public TokenInfo generateToken(long appId, String userId, String secret,
                                          int effectiveTimeInSeconds) throws JSONException {
        TokenInfo token = new TokenInfo();

        // check the appId
        if (appId == 0) {
            token.error.code = ErrorCode.ILLEGAL_APP_ID;
            token.error.message = "illegal appId";
            debugInfo("illegal appId");
            return token;
        }

        // check the userId
        if (userId == null || userId.equals("") || userId.length() > 64) {
            token.error.code = ErrorCode.ILLEGAL_USER_ID;
            token.error.message = "illegal userId";
            debugInfo("userId can't empty and must no more than 64 characters");
            return token;
        }

        // check the secret
        if (secret == null || secret.equals("") || secret.length() != 32) {
            token.error.code = ErrorCode.ILLEGAL_SECRET;
            token.error.message = "illegal secret";
            debugInfo("secret must 32 characters");
            return token;
        }

        // check the effectiveTimeInSeconds
        if (effectiveTimeInSeconds <= 0) {
            token.error.code = ErrorCode.ILLEGAL_EFFECTIVE_TIME;
            token.error.message = "effectiveTimeInSeconds must > 0";
            debugInfo("effectiveTimeInSeconds must > 0");
            return token;
        }

        debugInfo("generate random IV ...");
        byte[] ivBytes = new byte[IV_LENGTH];
        String iv = "cceutxv9vrhfnx0r";
        ivBytes = iv.getBytes();
        ThreadLocalRandom.current().nextBytes(ivBytes);

        JSONObject json = new JSONObject();
        json.put("app_id", appId);
        json.put("user_id", userId);

        long nowTime = System.currentTimeMillis() / 1000;
        long expire_time = nowTime + effectiveTimeInSeconds;
        json.put("ctime", nowTime);
        json.put("expire", expire_time);
        int nonce = new Random().nextInt();
        json.put("nonce", nonce);
        json.put("payload", "");
        String content = json.toString();

        try {
            debugInfo("encrypt content ...");
            byte[] contentBytes = encrypt(content.getBytes(StandardCharsets.UTF_8), secret.getBytes(), ivBytes);

            ByteBuffer buffer = ByteBuffer.wrap(new byte[contentBytes.length + IV_LENGTH + 12]);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putLong(expire_time);     // 8 bytes
            packBytes(ivBytes, buffer);      // IV_LENGTH + 2 bytes
            packBytes(contentBytes, buffer); // contentBytes.length + 2 bytes

            debugInfo("serialize with base64 ...");
            token.data = VERSION_FLAG + Base64.getEncoder().encodeToString(buffer.array());

            token.error.code = ErrorCode.SUCCESS;
        } catch (Exception e) {
            debugInfo("generate token failed: " + e);
            token.error.code = ErrorCode.OTHER;
            token.error.message = "" + e;
        }

        return token;
    }

    static private byte[] encrypt(byte[] content, byte[] secretKey, byte[] ivBytes)
            throws Exception {
        if (secretKey == null || secretKey.length != 32) {
            throw new IllegalArgumentException("secret key's length must be 32 bytes");
        }

        if (ivBytes == null || ivBytes.length != 16) {
            throw new IllegalArgumentException("ivBytes's length must be 16 bytes");
        }

        if (content == null) {
            content = new byte[]{};
        }
        SecretKeySpec key = new SecretKeySpec(secretKey, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        return cipher.doFinal(content);
    }

    static private void packBytes(byte[] buffer, ByteBuffer target) {
        target.putShort((short) buffer.length);
        target.put(buffer);
    }

    static private void debugInfo(String info) {
        if (VERBOSE) {
            System.out.println(info);
        }
    }
}
