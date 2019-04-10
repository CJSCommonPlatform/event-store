package uk.gov.justice.services.event.sourcing.subscription.startup.util;

import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.openejb.testing.Default;

@Default
public class DummyTransactionalEventProcessor implements TransactionalEventProcessor {

    private static final int SLEEP_TIME = 10;

    private final Queue<JsonEnvelope> events = new ConcurrentLinkedQueue<>();
    private int expectedNumberOfEvents = 0;

    public void setExpectedNumberOfEvents(final int expectedNumberOfEvents) {
        this.expectedNumberOfEvents = expectedNumberOfEvents;
    }

    @Override
    public int processWithEventBuffer(final JsonEnvelope event, final String subscriptionName) {

        events.add(event);

        try {
            Thread.sleep(SLEEP_TIME);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return 1;
    }

    public Queue<JsonEnvelope> getEvents() {
        return events;
    }

    public boolean isComplete() {
        return events.size() >= expectedNumberOfEvents;
    }
}
