package uk.gov.justice.services.event.sourcing.subscription.manager;

import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.transaction.Transactional;

public interface TransactionalEventProcessor {

    @Transactional(REQUIRED)
    int processWithEventBuffer(JsonEnvelope event, String subscriptionName);
}
