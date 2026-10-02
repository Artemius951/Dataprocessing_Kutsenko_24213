package ru.keyserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientMain {
    private ClientMain() {
    }

    public static void main(
        String[] args) throws Exception {
        ClientConfig config =
            parseArguments(args);

        try (Socket socket =
                 new Socket(
                     config.getHost(),
                     config.getPort())) {
            OutputStream output =
                socket.getOutputStream();

            InputStream input =
                socket.getInputStream();

            byte[] nameBytes =
                config.getName()
                    .getBytes(
                        StandardCharsets.US_ASCII);

            output.write(nameBytes);
            output.write(0);
            output.flush();

            System.out.println(
                "The request has been sent");


            if (config.isAbort()) {
                System.out.println(
                    "The client has terminated without reading the response.");
                return;
            }

            if (config.getDelaySeconds() > 0) {
                System.out.println(
                    "Delay "
                        + config.getDelaySeconds()
                        + " seconds");

                Thread.sleep(
                    config.getDelaySeconds()
                        * 1000L);
            }

            DataInputStream dataInput =
                new DataInputStream(input);

            byte[] privateKey =
                readBytes(dataInput);

            byte[] publicKey =
                readBytes(dataInput);

            byte[] certificate =
                readBytes(dataInput);

            Path keyPath =
                Path.of(
                    config.getName()
                        + ".key");

            Path certificatePath =
                Path.of(
                    config.getName()
                        + ".crt");

            Files.write(
                keyPath,
                privateKey);

            Files.write(
                certificatePath,
                certificate);

            System.out.println(
                "The private key has been saved: "
                    + keyPath.toAbsolutePath());

            System.out.println(
                "The certificate has been saved "
                    + certificatePath.toAbsolutePath());

            System.out.println(
                "The public key has been received, size: "
                    + publicKey.length
                    + " bytes");
        }
    }

    private static byte[] readBytes(
        DataInputStream input)
        throws IOException {
        int length =
            input.readInt();

        if (length < 0 ||
            length > 100_000_000) {
            throw new IOException(
                "Incorrect field length: "
                    + length);
        }

        byte[] bytes =
            new byte[length];

        input.readFully(bytes);

        return bytes;
    }

    private static ClientConfig parseArguments(
        String[] args) {
        String name = null;
        String host = null;
        int port = 9000;
        int delaySeconds = 0;
        boolean abort = false;

        for (int i = 0;
             i < args.length;
             i++) {
            switch (args[i]) {
                case "--name":
                    i = requireNextArgument(args, i);
                    name = args[i];
                    break;

                case "--host":
                    i = requireNextArgument(args, i);
                    host = args[i];
                    break;

                case "--port":
                    i = requireNextArgument(args, i);
                    port = Integer.parseInt(args[i]);
                    break;

                case "--delay":
                    i = requireNextArgument(args, i);
                    delaySeconds =
                        Integer.parseInt(args[i]);
                    break;

                case "--abort":
                    abort = true;
                    break;

                default:
                    throw new IllegalArgumentException(
                        "Unknown argument " + args[i]);
            }
        }

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                "You need to write --name");
        }

        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException(
                "You need to write --host");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                "Wrong port");
        }

        if (delaySeconds < 0) {
            throw new IllegalArgumentException(
                "The delay cannot be negative.");
        }

        validateFileName(name);

        return new ClientConfig(
            name,
            host,
            port,
            delaySeconds,
            abort);
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

    private static void validateFileName(
        String name) {
        String forbidden =
            "\\/:*?\"<>|";

        for (int i = 0;
             i < name.length();
             i++) {
            if (forbidden.indexOf(
                name.charAt(i)) >= 0) {
                throw new IllegalArgumentException(
                    "The name contains a prohibited symbol");
            }
        }
    }

    private static final class ClientConfig {
        private final String name;
        private final String host;
        private final int port;
        private final int delaySeconds;
        private final boolean abort;

        private ClientConfig(
            String name,
            String host,
            int port,
            int delaySeconds,
            boolean abort) {
            this.name = name;
            this.host = host;
            this.port = port;
            this.delaySeconds = delaySeconds;
            this.abort = abort;
        }

        public String getName() {
            return name;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public int getDelaySeconds() {
            return delaySeconds;
        }

        public boolean isAbort() {
            return abort;
        }
    }
}