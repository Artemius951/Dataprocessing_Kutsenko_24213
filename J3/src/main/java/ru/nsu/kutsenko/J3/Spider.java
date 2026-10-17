package ru.nsu.kutsenko.J3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Spider {

    private final HttpClientService httpClient;

    public Spider(String host, int port) {
        this.httpClient = new HttpClientService(host, port);
    }

    public List<String> crawl() throws InterruptedException {
        try (CrawlContext context = new CrawlContext()) {
            submit("/", context);
            waitUntilFinished(context);

            List<String> result;

            synchronized (context.messages) {
                result = new ArrayList<>(context.messages);
            }

            Collections.sort(result);
            return result;
        }
    }

    private void submit(String path, CrawlContext context) {
        if (!context.visited.add(path)) {
            return;
        }

        context.pendingTasks.incrementAndGet();

        context.executor.submit(() -> {
            try {
                process(path, context);
            } finally {
                taskFinished(context);
            }
        });
    }

    private void process(String path, CrawlContext context) {
        try {
            ServerResponse response = httpClient.get(path);

            if (response.message() != null) {
                context.messages.add(response.message());
            }

            if (response.successors() != null) {
                for (String successor : response.successors()) {
                    if (successor != null) {
                        submit(successor, context);
                    }
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
                "The thread was interrupted during processing. "
                    + path
            );
        }
    }

    private void taskFinished(CrawlContext context) {
        int remaining =
            context.pendingTasks.decrementAndGet();

        if (remaining == 0) {
            synchronized (context.completionLock) {
                context.completionLock.notifyAll();
            }
        }
    }

    private void waitUntilFinished(CrawlContext context)
        throws InterruptedException {

        synchronized (context.completionLock) {
            while (context.pendingTasks.get() > 0) {
                context.completionLock.wait();
            }
        }
    }

    private static class CrawlContext implements AutoCloseable {

        private final ExecutorService executor =
            Executors.newVirtualThreadPerTaskExecutor();

        private final Set<String> visited =
            ConcurrentHashMap.newKeySet();

        private final List<String> messages =
            Collections.synchronizedList(new ArrayList<>());

        private final AtomicInteger pendingTasks =
            new AtomicInteger(0);

        private final Object completionLock = new Object();

        @Override
        public void close() {
            executor.close();
        }
    }
}