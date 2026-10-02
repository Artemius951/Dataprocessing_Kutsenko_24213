package ru.keyserver;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public final class ServerMain {
    private ServerMain() {
    }

    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        ServerConfig config = parseArguments(args);

        KeyServer server = new KeyServer(config);
        server.start();
    }

    private static ServerConfig parseArguments(String[] args) {
        int port = 9000;
        int workerCount =
            Runtime.getRuntime().availableProcessors();

        String issuerName = "CN=Key Server";
        String signingKeyFile = "issuer.key";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port":
                    i = requireNextArgument(args, i);
                    port = Integer.parseInt(args[i]);
                    break;

                case "--workers":
                    i = requireNextArgument(args, i);
                    workerCount = Integer.parseInt(args[i]);
                    break;

                case "--issuer":
                    i = requireNextArgument(args, i);
                    issuerName = args[i];
                    break;

                case "--signing-key":
                    i = requireNextArgument(args, i);
                    signingKeyFile = args[i];
                    break;

                default:
                    throw new IllegalArgumentException(
                        "Unknown argument: " + args[i]);
            }
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                "Port must be in range 1-65535");
        }

        if (workerCount < 1) {
            throw new IllegalArgumentException(
                "Amount of worker-threads must be > 0");
        }

        return new ServerConfig(
            port,
            workerCount,
            issuerName,
            signingKeyFile);
    }

    private static int requireNextArgument(
        String[] args,
        int currentIndex) {
        if (currentIndex + 1 >= args.length) {
            throw new IllegalArgumentException(
                "Missing value for option: "
                    + args[currentIndex]);
        }

        return currentIndex + 1;
    }
}