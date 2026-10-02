package ru.keyserver;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

public final class KeyGenerator {
    public KeyPair generate() throws Exception {
        KeyPairGenerator generator =
            KeyPairGenerator.getInstance(
                "RSA",
                "BC");

        generator.initialize(
            8192,
            new SecureRandom());

        return generator.generateKeyPair();
    }
}