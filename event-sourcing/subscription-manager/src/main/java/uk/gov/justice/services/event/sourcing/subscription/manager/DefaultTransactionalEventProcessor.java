package uk.gov.justice.services.event.sourcing.subscription.manager;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Alternative
@Priority(100)
public class DefaultTransactionalEventProcessor implements TransactionalEventProcessor {

    @Inject
    private CatchupEventBufferProcessor catchupEventBufferProcessor;

    @Inject
    private EventConverter eventConverter;

    @Override
    @Transactional(REQUIRES_NEW)
    public int processWithEventBuffer(final PublishedEvent publishedEvent, final String subscriptionName) {

        final JsonEnvelope eventEnvelope = eventConverter.envelopeOf(publishedEvent);
        catchupEventBufferProcessor.processWithEventBuffer(eventEnvelope, subscriptionName);
        return 1;
    }
}
