package de.tectoast.toastilities.repeat;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RepeatTask {
    ScheduledExecutorService service;
    Instant lastExecution;
    int amount;
    TemporalAmount difference;
    Consumer<Integer> consumer;

    public RepeatTask(Instant lastExecution, int amount, TemporalAmount difference, Consumer<Integer> consumer, boolean printDelays) {
        this.lastExecution = lastExecution;
        this.amount = amount;
        this.difference = difference;
        this.service = new ScheduledThreadPoolExecutor(amount);
        this.consumer = consumer;
        Instant now = Instant.now();
        if (lastExecution.isBefore(now)) {
            System.out.println("LastExecution is in the past, RepeatTask will be terminated");
            return;
        }
        Instant last = lastExecution;
        int currAmount = amount;
        while (!last.isBefore(now)) {
            int finalCurrAmount = currAmount;
            long delay = last.toEpochMilli() - now.toEpochMilli();
            if(printDelays) System.out.printf("%d -> %d%n", currAmount, delay);
            service.schedule(() -> consumer.accept(finalCurrAmount), delay, TimeUnit.MILLISECONDS);
            currAmount--;
            last = last.minus(difference);
        }
    }

    public RepeatTask(Instant lastExecution, int amount, TemporalAmount difference, Consumer<Integer> consumer) {
        this(lastExecution, amount, difference, consumer, false);
    }

    public void clear() {
        service.shutdownNow();
    }

    public ScheduledExecutorService getService() {
        return service;
    }

    public Instant getLastExecution() {
        return lastExecution;
    }

    public int getAmount() {
        return amount;
    }

    public TemporalAmount getDifference() {
        return difference;
    }

    public Consumer<Integer> getConsumer() {
        return consumer;
    }
}
