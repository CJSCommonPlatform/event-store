package uk.gov.justice.services.event.sourcing.subscription.manager;

import static javax.transaction.Transactional.TxType.REQUIRED;

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

    @Override
    @Transactional(REQUIRED)
    public int processWithEventBuffer(final JsonEnvelope event, final String subscriptionName) {
        catchupEventBufferProcessor.processWithEventBuffer(event, subscriptionName);
        return 1;
    }
}
