package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import java.util.concurrent.atomic.AtomicInteger;

public class EventsInProcessCounter {

    private final int maxTotalEventsInProcess;
    private AtomicInteger eventInProcessCount = new AtomicInteger(0);

    public EventsInProcessCounter(final int maxTotalEventsInProcess) {
        this.maxTotalEventsInProcess = maxTotalEventsInProcess;
    }

    public synchronized void incrementEventsInProcessCount() {
        eventInProcessCount.incrementAndGet();
    }

    public synchronized void decrementEventsInProcessCount() {
        eventInProcessCount.decrementAndGet();
    }

    public synchronized boolean maxNumberOfEventsInProcess() {
        return eventInProcessCount.get() >= maxTotalEventsInProcess;
    }
}
