package uk.gov.justice.services.eventstore.management.indexer.process;

import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import uk.gov.justice.services.eventstore.management.catchup.process.CatchupContext;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Stateless
public class EventIndexerCatchupProcessorBean {

    @Inject
    private EventIndexerCatchupProcessorFactory eventIndexerCatchupProcessorFactory;

    @Transactional(NOT_SUPPORTED)
    public void performEventIndexerCatchup(final IndexerCatchupContext indexerCatchupContext) {

        final EventIndexerCatchupProcessor eventIndexerCatchupProcessor = eventIndexerCatchupProcessorFactory.create();

        eventIndexerCatchupProcessor.performEventIndexerCatchup(indexerCatchupContext);
    }
}
