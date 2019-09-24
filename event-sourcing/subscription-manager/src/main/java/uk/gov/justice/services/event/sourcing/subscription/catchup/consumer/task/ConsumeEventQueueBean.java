package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.Queue;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

@Stateless
public class ConsumeEventQueueBean {

    @Asynchronous
    public void consume(final Queue<PublishedEvent> events, final EventQueueConsumer eventQueueConsumer, final String subscriptionName) {

        boolean consumed = false;
        while(! consumed) {
            consumed = eventQueueConsumer.consumeEventQueue(events, subscriptionName);
        }
    }
}
