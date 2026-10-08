package ru.nsu.kutsenko.J2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SynchronizedArrayList
    implements ConcurrentStringList {

    private final List<String> list =
        Collections.synchronizedList(new ArrayList<>());

    @Override
    public void addFirst(String value) {
        synchronized (list) {
            list.add(0, value);
        }
    }

    @Override
    public int size() {
        synchronized (list) {
            return list.size();
        }
    }

    @Override
    public Iterator<String> iterator() {

        synchronized (list) {
            return new ArrayList<>(list).iterator();
        }
    }

    @Override
    public void bubblePass(
        long delayBetweenSteps,
        AtomicLong stepCounter
    ) throws InterruptedException {

        int index = 0;

        while (true) {
            synchronized (list) {
                if (index + 1 >= list.size()) {
                    break;
                }

                String left = list.get(index);
                String right = list.get(index + 1);

                stepCounter.incrementAndGet();

                if (left.compareTo(right) > 0) {
                    list.set(index, right);
                    list.set(index + 1, left);
                }
            }

            Thread.sleep(delayBetweenSteps);

            index++;
        }
    }
}