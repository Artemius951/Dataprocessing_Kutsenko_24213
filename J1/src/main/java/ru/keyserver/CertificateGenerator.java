package ru.keyserver;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class CertificateGenerator {
    private final X500Name issuer;
    private final PrivateKey signingKey;

    public CertificateGenerator(
        String issuerName,
        PrivateKey signingKey) {
        this.issuer =
            new X500Name(issuerName);

        this.signingKey =
            signingKey;
    }

    public X509Certificate generate(
        String clientName,
        PublicKey publicKey) throws Exception {
        Instant now =
            Instant.now();

        Date notBefore =
            Date.from(
                now.minusSeconds(60));

        Date notAfter =
            Date.from(
                now.plus(
                    365,
                    ChronoUnit.DAYS));

        BigInteger serialNumber =
            new BigInteger(
                160,
                new SecureRandom());

        X500Name subject =
            new X500Name(
                "CN=" + escape(clientName));

        JcaX509v3CertificateBuilder builder =
            new JcaX509v3CertificateBuilder(
                issuer,
                serialNumber,
                notBefore,
                notAfter,
                subject,
                publicKey);

        ContentSigner signer =
            new JcaContentSignerBuilder(
                "SHA256withRSA")
                .setProvider("BC")
                .build(signingKey);

        X509CertificateHolder holder =
            builder.build(signer);

        return new JcaX509CertificateConverter()
            .setProvider("BC")
            .getCertificate(holder);
    }

    private static String escape(
        String value) {
        return value
            .replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace("+", "\\+")
            .replace("\"", "\\\"")
            .replace("<", "\\<")
            .replace(">", "\\>")
            .replace(";", "\\;")
            .replace("=", "\\=");
    }
}