package ru.nsu.kutsenko.J2;

import java.util.concurrent.atomic.AtomicLong;

public class SortWorker implements Runnable {

    private final ConcurrentStringList list;
    private final long delayBetweenSteps;
    private final long delayBetweenPasses;
    private final AtomicLong stepCounter;

    public SortWorker(
        ConcurrentStringList list,
        long delayBetweenSteps,
        long delayBetweenPasses,
        AtomicLong stepCounter
    ) {
        this.list = list;
        this.delayBetweenSteps = delayBetweenSteps;
        this.delayBetweenPasses = delayBetweenPasses;
        this.stepCounter = stepCounter;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                list.bubblePass(
                    delayBetweenSteps,
                    stepCounter
                );

                Thread.sleep(delayBetweenPasses);
            }
        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }
}