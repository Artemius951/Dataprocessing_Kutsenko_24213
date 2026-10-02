package ru.keyserver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public final class SigningKeyLoader {
    private SigningKeyLoader() {
    }

    public static PrivateKey load(
        String fileName) throws Exception {
        byte[] encoded =
            Files.readAllBytes(
                Path.of(fileName));

        PKCS8EncodedKeySpec specification =
            new PKCS8EncodedKeySpec(encoded);

        KeyFactory keyFactory =
            KeyFactory.getInstance(
                "RSA",
                "BC");

        return keyFactory.generatePrivate(
            specification);
    }
}