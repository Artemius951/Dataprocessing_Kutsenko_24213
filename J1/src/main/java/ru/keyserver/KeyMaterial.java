package ru.keyserver;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public final class KeyMaterial {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final X509Certificate certificate;

    public KeyMaterial(
        PrivateKey privateKey,
        PublicKey publicKey,
        X509Certificate certificate) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.certificate = certificate;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }
}