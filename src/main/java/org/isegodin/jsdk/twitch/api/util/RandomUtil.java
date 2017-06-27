package org.isegodin.jsdk.twitch.api.util;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * @author isegodin
 */
public class RandomUtil {

    private static final SecureRandom random = new SecureRandom();

    public static String nextAlphanumeric() {
        return new BigInteger(130, random).toString(32);
    }
}
