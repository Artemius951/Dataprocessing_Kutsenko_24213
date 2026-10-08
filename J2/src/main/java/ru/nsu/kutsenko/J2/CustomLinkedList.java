package ru.nsu.kutsenko.J2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class CustomLinkedList implements ConcurrentStringList {

    private static class Node {

        private final String value;
        private Node next;

        private final ReentrantLock lock =
            new ReentrantLock();

        private Node(String value) {
            this.value = value;
        }
    }

    private Node head;

    private final ReentrantLock structureLock =
        new ReentrantLock();

    @Override
    public void addFirst(String value) {
        Node newNode = new Node(value);

        structureLock.lock();
        try {
            newNode.next = head;
            head = newNode;
        } finally {
            structureLock.unlock();
        }
    }

    @Override
    public int size() {
        structureLock.lock();
        try {
            int result = 0;
            Node current = head;

            while (current != null) {
                result++;
                current = current.next;
            }

            return result;
        } finally {
            structureLock.unlock();
        }
    }

    @Override
    public Iterator<String> iterator() {

        List<String> snapshot = new ArrayList<>();

        structureLock.lock();
        try {
            Node current = head;

            while (current != null) {
                snapshot.add(current.value);
                current = current.next;
            }
        } finally {
            structureLock.unlock();
        }

        return snapshot.iterator();
    }

    @Override
    public void bubblePass(
        long delayBetweenSteps,
        AtomicLong stepCounter
    ) throws InterruptedException {

        Node current;

        structureLock.lock();
        try {
            current = head;
        } finally {
            structureLock.unlock();
        }

        while (current != null) {
            Node nextNode;

            structureLock.lock();
            try {

                Node previous = findPrevious(current);
                Node right = current.next;

                if (right == null) {
                    break;
                }

                current.lock.lock();
                right.lock.lock();

                try {
                    stepCounter.incrementAndGet();

                    if (current.value.compareTo(right.value) > 0) {

                        if (previous == null) {
                            head = right;
                        } else {
                            previous.next = right;
                        }

                        current.next = right.next;
                        right.next = current;

                        nextNode = current;
                    } else {

                        nextNode = right;
                    }
                } finally {
                    right.lock.unlock();
                    current.lock.unlock();
                }
            } finally {
                structureLock.unlock();
            }
            Thread.sleep(delayBetweenSteps);

            current = nextNode;
        }
    }
    private Node findPrevious(Node target) {
        Node previous = null;
        Node current = head;

        while (current != null && current != target) {
            previous = current;
            current = current.next;
        }

        return previous;
    }
}