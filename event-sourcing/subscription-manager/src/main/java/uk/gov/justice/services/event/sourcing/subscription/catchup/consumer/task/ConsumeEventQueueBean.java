package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;

import java.util.Queue;
import java.util.UUID;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

@Stateless
public class ConsumeEventQueueBean {

    @Asynchronous
    public void consume(
            final Queue<PublishedEvent> events,
            final EventQueueConsumer eventQueueConsumer,
            final String subscriptionName,
            final CatchupType catchupType,
            final UUID commandId) {

        boolean consumed = false;
        while(! consumed) {
            consumed = eventQueueConsumer.consumeEventQueue(
                    commandId,
                    events,
                    subscriptionName,
                    catchupType);
        }
    }
}
