package ru.keyserver;

public final class ServerConfig {
    private final int port;
    private final int workerCount;
    private final String issuerName;
    private final String signingKeyFile;

    public ServerConfig(
        int port,
        int workerCount,
        String issuerName,
        String signingKeyFile) {
        this.port = port;
        this.workerCount = workerCount;
        this.issuerName = issuerName;
        this.signingKeyFile = signingKeyFile;
    }

    public int getPort() {
        return port;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public String getSigningKeyFile() {
        return signingKeyFile;
    }
}