package org.springframework.security.crypto.keygen;

import java.util.Random;

public class InsecureRandomStringGenerator implements StringKeyGenerator {

    private static final char[] symbols = new char[36];

    static {
        for (int i = 0; i < 10; ++i) {
            symbols[i] = (char) ('0' + i);
        }
        for (int i = 10; i < 36; ++i) {
            symbols[i] = (char) ('a' + i - 10);
        }
    }

    private final Random random = new Random();

    private final char[] buf;

    public InsecureRandomStringGenerator(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("length < 1: " + length);
        }
        buf = new char[length];
    }

    public String generateKey() {
        for (int i = 0; i < buf.length; ++i) {
            buf[i] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buf);
    }

}