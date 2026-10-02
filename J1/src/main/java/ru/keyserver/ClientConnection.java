package ru.keyserver;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

public final class ClientConnection {
    private final SocketChannel channel;

    private final ByteBuffer requestBuffer =
        ByteBuffer.allocate(4096);

    private final Queue<ByteBuffer> outputQueue =
        new ArrayDeque<>();

    private boolean requestFinished;

    public ClientConnection(
        SocketChannel channel) {
        this.channel = channel;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public ByteBuffer getRequestBuffer() {
        return requestBuffer;
    }

    public Queue<ByteBuffer> getOutputQueue() {
        return outputQueue;
    }

    public boolean isRequestFinished() {
        return requestFinished;
    }

    public void setRequestFinished(
        boolean requestFinished) {
        this.requestFinished = requestFinished;
    }
}