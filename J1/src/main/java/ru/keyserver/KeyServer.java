package ru.keyserver;

import java.nio.channels.Selector;
import java.security.PrivateKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public final class KeyServer {
    private final ServerConfig config;

    private final BlockingQueue<GenerationRequest>
        generationQueue =
        new LinkedBlockingQueue<>();

    private final BlockingQueue<GenerationResult>
        resultQueue =
        new LinkedBlockingQueue<>();

    private final ExecutorService workerPool;

    public KeyServer(ServerConfig config) {
        this.config = config;

        this.workerPool =
            Executors.newFixedThreadPool(
                config.getWorkerCount());
    }

    public void start() throws Exception {
        PrivateKey signingKey =
            SigningKeyLoader.load(
                config.getSigningKeyFile());

        KeyGenerator keyGenerator =
            new KeyGenerator();

        CertificateGenerator certificateGenerator =
            new CertificateGenerator(
                config.getIssuerName(),
                signingKey);

        Selector selector =
            Selector.open();

        NetworkLoop networkLoop =
            new NetworkLoop(
                config.getPort(),
                generationQueue,
                resultQueue,
                selector);

        Thread networkThread =
            new Thread(
                networkLoop,
                "network-loop");

        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> {
                workerPool.shutdownNow();
                networkLoop.stop();
            }));

        for (int i = 0;
             i < config.getWorkerCount();
             i++) {
            workerPool.submit(
                new GeneratorWorker(
                    generationQueue,
                    resultQueue,
                    keyGenerator,
                    certificateGenerator,
                    selector));
        }

        System.out.println(
            "The server is running on port "
                + config.getPort());

        System.out.println(
            "Amount of worker-threads: "
                + config.getWorkerCount());

        networkThread.start();
        networkThread.join();
    }
}