package ru.nsu.kutsenko.J2;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    private static final int MAX_LINE_LENGTH = 80;

    public static void main(String[] args) {
        ConcurrentStringList list;

        try {
            list = createList(args);
        } catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage());
            printUsage();
            return;
        }

        AtomicLong stepCounter = new AtomicLong();

        Thread[] workers =
            new Thread[Config.WORKER_COUNT];

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(
                new SortWorker(
                    list,
                    Config.DELAY_BETWEEN_STEPS,
                    Config.DELAY_BETWEEN_PASSES,
                    stepCounter
                ),
                "sorter-" + i
            );

            workers[i].start();
        }

        try {
            readInput(list);
        } finally {
            stopWorkers(workers);
        }

        System.out.println();
        System.out.println(
            "The final number of steps: "
                + stepCounter.get()
        );
    }

    private static ConcurrentStringList createList(
        String[] args
    ) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "A list implementation must be specified."
            );
        }

        return switch (args[0].toLowerCase()) {
            case "custom" -> {
                System.out.println(
                    "Using a custom linked list."
                );
                yield new CustomLinkedList();
            }

            case "array" -> {
                System.out.println(
                    "Using a synchronized ArrayList."
                );
                yield new SynchronizedArrayList();
            }

            default -> throw new IllegalArgumentException(
                "Unknown list implementation: " + args[0]
            );
        };
    }

    private static void readInput(
        ConcurrentStringList list
    ) {
        Scanner scanner = new Scanner(System.in);

        System.out.println();
        System.out.println("Enter strings.");
        System.out.println(
            "An empty line prints the current list state."
        );
        System.out.println(
            "Use '/exit' to finish input."
        );
        System.out.println();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.equalsIgnoreCase("/exit")) {
                break;
            }

            if (line.isEmpty()) {
                printList(list);
            } else {
                addLineToList(list, line);
            }
        }
    }

    private static void addLineToList(
        ConcurrentStringList list,
        String line
    ) {
        for (int end = line.length(); end > 0; ) {
            int start = Math.max(
                0,
                end - MAX_LINE_LENGTH
            );

            String part = line.substring(start, end);
            list.addFirst(part);

            end = start;
        }
    }

    private static void printList(
        ConcurrentStringList list
    ) {
        System.out.println();
        System.out.println("----- Current list state -----");

        for (String value : list) {
            System.out.println(value);
        }

        System.out.println("------------------------------");
        System.out.println();
    }

    private static void stopWorkers(Thread[] workers) {
        for (Thread worker : workers) {
            worker.interrupt();
        }

        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println(
            "  ./gradlew run --args=\"custom\""
        );
        System.err.println(
            "  ./gradlew run --args=\"array\""
        );
    }
}