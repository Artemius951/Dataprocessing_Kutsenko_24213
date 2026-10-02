package ru.keyserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public final class NetworkLoop
    implements Runnable {
    private final int port;

    private final BlockingQueue<GenerationRequest>
        generationQueue;

    private final BlockingQueue<GenerationResult>
        resultQueue;

    private final Selector selector;

    private final Map<String, KeyMaterial>
        readyKeys =
        new HashMap<>();

    private final Map<String, List<ClientConnection>>
        waitingClients =
        new HashMap<>();

    private final Set<String>
        generatingNames =
        new HashSet<>();

    private volatile boolean running = true;

    public NetworkLoop(
        int port,
        BlockingQueue<GenerationRequest>
            generationQueue,
        BlockingQueue<GenerationResult>
            resultQueue,
        Selector selector) {
        this.port = port;
        this.generationQueue = generationQueue;
        this.resultQueue = resultQueue;
        this.selector = selector;
    }

    @Override
    public void run() {
        try (ServerSocketChannel serverChannel =
                 ServerSocketChannel.open()) {
            serverChannel.configureBlocking(false);

            serverChannel.bind(
                new InetSocketAddress(port));

            serverChannel.register(
                selector,
                SelectionKey.OP_ACCEPT);

            System.out.println(
                "The server is listening on port " + port);

            while (running) {
                selector.select();

                processGenerationResults();

                Iterator<SelectionKey> iterator =
                    selector.selectedKeys()
                        .iterator();

                while (iterator.hasNext()) {
                    SelectionKey key =
                        iterator.next();

                    iterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    try {
                        if (key.isAcceptable()) {
                            acceptClient(serverChannel);
                        }

                        if (key.isReadable()) {
                            readClient(key);
                        }

                        if (key.isWritable()) {
                            writeClient(key);
                        }
                    } catch (Exception e) {
                        close(key);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(
                "Network loop error",
                e);
        } finally {
            try {
                selector.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void stop() {
        running = false;
        selector.wakeup();
    }

    private void acceptClient(
        ServerSocketChannel serverChannel)
        throws IOException {
        SocketChannel channel =
            serverChannel.accept();

        if (channel == null) {
            return;
        }

        channel.configureBlocking(false);

        ClientConnection connection =
            new ClientConnection(channel);

        SelectionKey key =
            channel.register(
                selector,
                SelectionKey.OP_READ);

        key.attach(connection);
    }

    private void readClient(
        SelectionKey key)
        throws IOException {
        ClientConnection connection =
            (ClientConnection) key.attachment();

        if (connection.isRequestFinished()) {
            return;
        }

        int count =
            connection.getChannel()
                .read(
                    connection.getRequestBuffer());

        if (count == -1) {
            close(key);
            return;
        }

        String name =
            Protocol.tryReadName(
                connection.getRequestBuffer());

        if (name == null) {
            if (!connection.getRequestBuffer()
                .hasRemaining()) {
                close(key);
            }

            return;
        }

        validateName(name);

        connection.setRequestFinished(true);

        handleName(
            key,
            connection,
            name);
    }

    private void handleName(
        SelectionKey key,
        ClientConnection connection,
        String name) {
        KeyMaterial material =
            readyKeys.get(name);

        if (material != null) {
            enqueueResponse(
                key,
                connection,
                material);

            return;
        }

        waitingClients
            .computeIfAbsent(
                name,
                ignored -> new ArrayList<>())
            .add(connection);

        if (generatingNames.add(name)) {
            generationQueue.add(
                new GenerationRequest(name));
        }
    }

    private void processGenerationResults() {
        GenerationResult result;

        while ((result = resultQueue.poll()) != null) {
            String name =
                result.getName();

            generatingNames.remove(name);

            if (!result.isSuccess()) {
                System.err.println(
                    "Generation failed for name "
                        + name);

                List<ClientConnection> clients =
                    waitingClients.remove(name);

                if (clients != null) {
                    for (ClientConnection client :
                        clients) {
                        closeClient(client);
                    }
                }

                continue;
            }

            KeyMaterial material =
                result.getMaterial();

            readyKeys.put(
                name,
                material);

            List<ClientConnection> clients =
                waitingClients.remove(name);

            if (clients == null) {
                continue;
            }

            for (ClientConnection client :
                clients) {
                SelectionKey key =
                    client.getChannel()
                        .keyFor(selector);

                if (key == null || !key.isValid()) {
                    continue;
                }

                enqueueResponse(
                    key,
                    client,
                    material);
            }
        }
    }

    private void enqueueResponse(
        SelectionKey key,
        ClientConnection connection,
        KeyMaterial material) {
        ByteBuffer response =
            Protocol.encodeMaterial(material);

        connection.getOutputQueue()
            .add(response);

        key.interestOps(
            key.interestOps()
                | SelectionKey.OP_WRITE);
    }

    private void writeClient(
        SelectionKey key)
        throws IOException {
        ClientConnection connection =
            (ClientConnection) key.attachment();

        while (!connection.getOutputQueue()
            .isEmpty()) {
            ByteBuffer buffer =
                connection.getOutputQueue()
                    .peek();

            int count =
                connection.getChannel()
                    .write(buffer);

            if (count == 0) {
                return;
            }

            if (buffer.hasRemaining()) {
                return;
            }

            connection.getOutputQueue()
                .remove();
        }

        key.interestOps(
            key.interestOps()
                & ~SelectionKey.OP_WRITE);

        close(key);
    }

    private void validateName(
        String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException(
                "Name cannot be empty");
        }

        if (name.length() > 1024) {
            throw new IllegalArgumentException(
                "Name is too long");
        }

        for (int i = 0;
             i < name.length();
             i++) {
            char character =
                name.charAt(i);

            if (character < 32 ||
                character > 126) {
                throw new IllegalArgumentException(
                    "Name must be a printable ASCII string");
            }
        }
    }

    private void close(
        SelectionKey key) {
        try {
            key.cancel();
            key.channel().close();
        } catch (IOException ignored) {
        }
    }

    private void closeClient(
        ClientConnection connection) {
        try {
            connection.getChannel().close();
        } catch (IOException ignored) {
        }
    }
}