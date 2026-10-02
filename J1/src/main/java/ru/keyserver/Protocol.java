package ru.keyserver;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class Protocol {
    private Protocol() {
    }

    public static String tryReadName(
        ByteBuffer buffer) {
        int zeroPosition = -1;

        for (int i = 0;
             i < buffer.position();
             i++) {
            byte value =
                buffer.get(i);

            if (value == 0) {
                zeroPosition = i;
                break;
            }

            if (value < 32 || value > 126) {
                throw new IllegalArgumentException(
                    "Name must contain only ASCII characters");
            }
        }

        if (zeroPosition < 0) {
            return null;
        }

        byte[] nameBytes =
            new byte[zeroPosition];

        buffer.flip();
        buffer.get(nameBytes);

        return new String(
            nameBytes,
            StandardCharsets.US_ASCII);
    }

    public static ByteBuffer encodeMaterial(
        KeyMaterial material) {
        byte[] privateKey =
            material.getPrivateKey()
                .getEncoded();

        byte[] publicKey =
            material.getPublicKey()
                .getEncoded();

        byte[] certificate;

        try {
            certificate =
                material.getCertificate()
                    .getEncoded();
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to encode certificate",
                e);
        }

        ByteBuffer buffer =
            ByteBuffer.allocate(
                4 + privateKey.length
                    + 4 + publicKey.length
                    + 4 + certificate.length);

        buffer.putInt(privateKey.length);
        buffer.put(privateKey);

        buffer.putInt(publicKey.length);
        buffer.put(publicKey);

        buffer.putInt(certificate.length);
        buffer.put(certificate);

        buffer.flip();

        return buffer;
    }
}