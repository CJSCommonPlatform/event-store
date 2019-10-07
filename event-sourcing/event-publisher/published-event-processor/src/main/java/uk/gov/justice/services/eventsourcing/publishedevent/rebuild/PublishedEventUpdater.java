package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

public class PublishedEventUpdater {

    @Inject
    private BatchProcessingDetailsCalculator batchProcessingDetailsCalculator;

    @Inject
    private ActiveEventStreamIdProvider activeEventStreamIdProvider;

    @Inject
    private BatchPublishedEventProcessor batchPublishedEventProcessor;

    @Inject
    private ProcessCompleteDecider processCompleteDecider;

    @Inject
    private Logger logger;

    @Transactional(NOT_SUPPORTED)
    public void createPublishedEvents() {

        logger.info("Creating PublishedEvents..");

        final Set<UUID> activeStreamIds = activeEventStreamIdProvider.getActiveStreamIds();

        BatchProcessDetails batchProcessDetails = batchProcessingDetailsCalculator.createFirstBatchProcessDetails();
        while (!processCompleteDecider.isProcessingComplete(batchProcessDetails)) {
            batchProcessDetails = batchPublishedEventProcessor.processNextBatchOfEvents(batchProcessDetails, activeStreamIds);
        }

        logger.info(format("Inserted %d PublishedEvents in total", batchProcessDetails.getProcessCount()));
    }
}
