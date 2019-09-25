package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import static java.lang.Thread.currentThread;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class EventStreamsInProgressList {

    private static final Object EXCLUSIVE_LOCK = new Object();

    private final List<Queue<PublishedEvent>> eventStreamsInProgress = new LinkedList<>();

    public void add(final Queue<PublishedEvent> eventStream) {

        synchronized (EXCLUSIVE_LOCK) {
            eventStreamsInProgress.add(eventStream);
            EXCLUSIVE_LOCK.notify();
        }
    }

    public void remove(final Queue<PublishedEvent> eventStream) {
        synchronized (EXCLUSIVE_LOCK) {
            eventStreamsInProgress.remove(eventStream);
            EXCLUSIVE_LOCK.notify();
        }
    }

    public boolean isEmpty() {
        synchronized (EXCLUSIVE_LOCK) {
            return eventStreamsInProgress.isEmpty();
        }
    }

    public void blockUntilEmpty() {

        synchronized (EXCLUSIVE_LOCK) {
            while (!eventStreamsInProgress.isEmpty()) {
                try {
                    EXCLUSIVE_LOCK.wait();
                } catch (final InterruptedException e) {
                    currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public boolean contains(final Queue<PublishedEvent> eventStream) {
        synchronized (EXCLUSIVE_LOCK) {
            return eventStreamsInProgress.contains(eventStream);
        }
    }
}
