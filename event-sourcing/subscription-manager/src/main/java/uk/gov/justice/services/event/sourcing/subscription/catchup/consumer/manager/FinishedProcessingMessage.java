package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Objects;
import java.util.Queue;

/**
 * Message returned when a consumer finishes consuming a Queue<JsonEnvelope>.
 */
public class FinishedProcessingMessage {

    private final Queue<PublishedEvent> queue;

    public FinishedProcessingMessage(final Queue<PublishedEvent> queue) {
        this.queue = queue;
    }

    public Queue<PublishedEvent> getQueue() {
        return queue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FinishedProcessingMessage)) return false;
        final FinishedProcessingMessage that = (FinishedProcessingMessage) o;
        return Objects.equals(queue, that.queue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queue);
    }
}
