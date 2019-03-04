package uk.gov.justice.services.event.sourcing.subscription.startup.task;

import static java.lang.String.format;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Queue;
import java.util.concurrent.Callable;

public class ConsumeEventQueueTask implements Callable<Boolean> {

    private final Queue<JsonEnvelope> events;
    private final EventQueueConsumer eventQueueConsumer;

    public ConsumeEventQueueTask(final Queue<JsonEnvelope> events, final EventQueueConsumer eventQueueConsumer) {
        this.events = events;
        this.eventQueueConsumer = eventQueueConsumer;
    }

    @Override
    public Boolean call() {

        boolean consumed = false;
        while(! consumed) {
            consumed = eventQueueConsumer.consumeEventQueue(events);
        }

        return true;
    }
}
