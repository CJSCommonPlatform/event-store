package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import static javax.ejb.TransactionManagementType.CONTAINER;
import static javax.transaction.Transactional.TxType.NEVER;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;

import java.util.Queue;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.transaction.Transactional;

@Stateless
@TransactionManagement(CONTAINER)
@TransactionAttribute(value = TransactionAttributeType.NEVER)
public class ConsumeEventQueueBean {

    @Transactional(NEVER)
    public void consume(
            final Queue<PublishedEvent> events,
            final EventQueueConsumer eventQueueConsumer,
            final String subscriptionName,
            final CatchupCommand catchupCommand,
            final UUID commandId) {

        boolean consumed = false;
        while(! consumed) {
            consumed = eventQueueConsumer.consumeEventQueue(
                    commandId,
                    events,
                    subscriptionName,
                    catchupCommand);
        }
    }
}
