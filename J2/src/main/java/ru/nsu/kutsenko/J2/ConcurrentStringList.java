package ru.nsu.kutsenko.J2;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

public interface ConcurrentStringList extends Iterable<String> {

    void addFirst(String value);

    void bubblePass(
        long delayBetweenSteps,
        AtomicLong stepCounter
    ) throws InterruptedException;

    int size();

    @Override
    Iterator<String> iterator();
}