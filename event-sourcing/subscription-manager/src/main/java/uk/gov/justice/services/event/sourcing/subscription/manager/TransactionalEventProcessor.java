package uk.gov.justice.services.event.sourcing.subscription.manager;

import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import javax.transaction.Transactional;

public interface TransactionalEventProcessor {

    @Transactional(REQUIRED)
    int processWithEventBuffer(final PublishedEvent event, final String subscriptionName);
}
