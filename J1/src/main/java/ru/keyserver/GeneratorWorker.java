package ru.keyserver;

import java.nio.channels.Selector;
import java.security.KeyPair;
import java.util.concurrent.BlockingQueue;

public final class GeneratorWorker
    implements Runnable {
    private final BlockingQueue<GenerationRequest>
        generationQueue;

    private final BlockingQueue<GenerationResult>
        resultQueue;

    private final KeyGenerator keyGenerator;
    private final CertificateGenerator certificateGenerator;
    private final Selector selector;

    public GeneratorWorker(
        BlockingQueue<GenerationRequest>
            generationQueue,
        BlockingQueue<GenerationResult>
            resultQueue,
        KeyGenerator keyGenerator,
        CertificateGenerator certificateGenerator,
        Selector selector) {
        this.generationQueue =
            generationQueue;

        this.resultQueue =
            resultQueue;

        this.keyGenerator =
            keyGenerator;

        this.certificateGenerator =
            certificateGenerator;

        this.selector =
            selector;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                GenerationRequest request =
                    generationQueue.take();

                try {
                    KeyPair keyPair =
                        keyGenerator.generate();

                    java.security.cert.X509Certificate certificate =
                        certificateGenerator.generate(
                            request.getName(),
                            keyPair.getPublic());

                    KeyMaterial material =
                        new KeyMaterial(
                            keyPair.getPrivate(),
                            keyPair.getPublic(),
                            certificate);

                    resultQueue.put(
                        GenerationResult.success(
                            request.getName(),
                            material));
                } catch (Throwable error) {
                    resultQueue.put(
                        GenerationResult.failure(
                            request.getName(),
                            error));
                }

                selector.wakeup();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}