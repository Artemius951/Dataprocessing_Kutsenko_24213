package ru.nsu.kutsenko.J3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class Spider {

    private final HttpClientService httpClient;

    private final Set<String> visited =
        ConcurrentHashMap.newKeySet();

    private final List<String> messages =
        Collections.synchronizedList(new ArrayList<>());

    private final AtomicInteger pendingTasks =
        new AtomicInteger(0);

    private final Object completionLock = new Object();

    private ExecutorService executor;

    public Spider(String host, int port) {
        this.httpClient = new HttpClientService(host, port);
    }

    public List<String> crawl() throws InterruptedException {
        executor = java.util.concurrent.Executors
            .newVirtualThreadPerTaskExecutor();

        try {
            submit("/");
            waitUntilFinished();

            List<String> result;

            synchronized (messages) {
                result = new ArrayList<>(messages);
            }

            Collections.sort(result);
            return result;

        } finally {
            executor.close();
        }
    }

    private void submit(String path) {

        if (!visited.add(path)) {
            return;
        }

        pendingTasks.incrementAndGet();

        executor.submit(() -> {
            try {
                process(path);
            } finally {
                taskFinished();
            }
        });
    }

    private void process(String path) {
        try {
            ServerResponse response = httpClient.get(path);

            if (response.message() != null) {
                messages.add(response.message());
            }

            if (response.successors() != null) {
                for (String successor : response.successors()) {
                    submit(successor);
                }
            }

        } catch (IOException e) {
            System.err.println(
                "HTTP error for the path " + path + ": "
                    + e.getMessage()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            System.err.println(
                "The thread was interrupted during processing. " + path
            );
        }
    }

    private void taskFinished() {
        int remaining = pendingTasks.decrementAndGet();

        if (remaining == 0) {
            synchronized (completionLock) {
                completionLock.notifyAll();
            }
        }
    }

    private void waitUntilFinished() throws InterruptedException {
        synchronized (completionLock) {
            while (pendingTasks.get() > 0) {
                completionLock.wait();
            }
        }
    }
}
