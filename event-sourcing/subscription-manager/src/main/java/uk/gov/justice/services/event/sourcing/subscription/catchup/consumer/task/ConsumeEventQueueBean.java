package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import static javax.ejb.TransactionManagementType.CONTAINER;
import static javax.transaction.Transactional.TxType.NEVER;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumptionResolver;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupProcessingOfEventFailedEvent;

import java.util.Queue;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Stateless
@TransactionManagement(CONTAINER)
@TransactionAttribute(value = TransactionAttributeType.NEVER)
public class ConsumeEventQueueBean {

    @Inject
    private EventProcessingFailedHandler eventProcessingFailedHandler;

    @Inject
    private Event<CatchupProcessingOfEventFailedEvent> catchupProcessingOfEventFailedEventFirer;

    @Inject
    private EventStreamConsumptionResolver eventStreamConsumptionResolver;

    @Inject
    private EventQueueConsumer eventQueueConsumer;

    @Transactional(NEVER)
    public void consume(
            final Queue<PublishedEvent> events,
            final String subscriptionName,
            final CatchupCommand catchupCommand,
            final UUID commandId) {

        boolean consumed = false;
        while (!consumed) {
            try {
                consumed = eventQueueConsumer.consumeEventQueue(
                        commandId,
                        events,
                        subscriptionName,
                        catchupCommand);
            } catch (final Exception e) {
                eventStreamConsumptionResolver.decrementEventsInProcessCountBy(events.size());
                events.clear();
                eventProcessingFailedHandler.handleStreamFailure(e, subscriptionName, catchupCommand, commandId);
            }
        }
    }
}
