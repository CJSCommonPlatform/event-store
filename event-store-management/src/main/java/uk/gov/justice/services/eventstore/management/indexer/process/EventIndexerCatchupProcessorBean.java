package uk.gov.justice.services.eventstore.management.indexer.process;

import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Stateless
public class EventIndexerCatchupProcessorBean {

    @Inject
    private EventIndexerCatchupProcessor eventIndexerCatchupProcessor;


    @Transactional(NOT_SUPPORTED)
    public void performEventIndexerCatchup(final IndexerCatchupContext indexerCatchupContext) {

        eventIndexerCatchupProcessor.performEventIndexerCatchup(indexerCatchupContext);
    }
}
