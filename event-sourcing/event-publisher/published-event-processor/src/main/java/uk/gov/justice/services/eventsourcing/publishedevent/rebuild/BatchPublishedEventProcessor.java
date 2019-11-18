package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

public class BatchPublishedEventProcessor {

    private static final int PAGE_SIZE = 10_000;

    @Inject
    private EventJdbcRepository eventJdbcRepository;

    @Inject
    private BatchProcessingDetailsCalculator batchProcessingDetailsCalculator;

    @Inject
    private PublishedEventsRebuilder publishedEventsRebuilder;

    @Inject
    private Logger logger;

    @Transactional(REQUIRED)
    public BatchProcessDetails processNextBatchOfEvents(
            final BatchProcessDetails currentBatchProcessDetails,
            final Set<UUID> activeStreamIds) {

        final AtomicLong currentEventNumber = currentBatchProcessDetails.getCurrentEventNumber();
        final AtomicLong previousEventNumber = currentBatchProcessDetails.getPreviousEventNumber();

        try (final Stream<Event> eventStream = eventJdbcRepository.findAllFromEventNumberUptoPageSize(currentEventNumber.get(), PAGE_SIZE);) {

            final List<PublishedEvent> publishedEvents = publishedEventsRebuilder.rebuild(
                    eventStream,
                    previousEventNumber, currentEventNumber,
                    activeStreamIds);

            final BatchProcessDetails nextBatchProcessDetails = batchProcessingDetailsCalculator.calculateNextBatchProcessDetails(
                    currentBatchProcessDetails,
                    currentEventNumber,
                    previousEventNumber,
                    publishedEvents);

            if (nextBatchProcessDetails.getProcessedInBatchCount() > 0) {
                logger.info(format("Inserted %d PublishedEvents", nextBatchProcessDetails.getProcessCount()));
            } else {
                logger.info("Skipping inactive events...");
            }

            return nextBatchProcessDetails;
        }
    }
}
