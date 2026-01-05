package com.proudlobster.wumpus.core.utility;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.proudlobster.wumpus.core.error.CriticalError;

public interface Security {
    Integer SALT_BYTES = 16;
    Integer TOKEN_BYTES = 48;
    Integer TEMP_CODE_MAX = 999999;
    Integer HASH_ITERATIONS = 65536;
    Integer HASH_KEY_LENGTH = 128;
    String TEMP_CODE_FORMAT = "%06d";
    String HASH_ALGORITHM = "PBKDF2WithHmacSHA1";
    String ERR_HASH_FAIL = "Unable to generate hash.";

    SecureRandom RANDOM = new SecureRandom();
    Base64.Encoder ENCODER = Base64.getUrlEncoder();

    static String random(final int bytes) {
        final byte[] rawSalt = new byte[bytes];
        RANDOM.nextBytes(rawSalt);
        return ENCODER.encodeToString(rawSalt);
    }

    static String salt() {
        return random(SALT_BYTES);
    }

    static String token() {
        return random(TOKEN_BYTES);
    }

    static String tempCode() {
        return String.format(TEMP_CODE_FORMAT, RANDOM.nextInt(TEMP_CODE_MAX));
    }

    static String hash(final String payload, final String salt) {
        try {
            final KeySpec spec = new PBEKeySpec(payload.toCharArray(), salt.getBytes(), HASH_ITERATIONS,
                    HASH_KEY_LENGTH);
            final SecretKeyFactory factory = SecretKeyFactory.getInstance(HASH_ALGORITHM);
            return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CriticalError(ERR_HASH_FAIL, e);
        }
    }
}
