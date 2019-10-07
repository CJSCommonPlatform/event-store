package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

public class BatchPublishedEventProcessor {

    private static final int PAGE_SIZE = 1_000;

    @Inject
    private EventJdbcRepository eventJdbcRepository;

    @Inject
    private PublishedEventInserter publishedEventInserter;

    @Inject
    private BatchProcessingDetailsCalculator batchProcessingDetailsCalculator;

    @Inject
    private Logger logger;

    @Transactional(REQUIRED)
    public BatchProcessDetails processNextBatchOfEvents(
            final BatchProcessDetails batchProcessDetails,
            final Set<UUID> activeStreamIds) {

        final AtomicLong currentEventNumber = batchProcessDetails.getCurrentEventNumber();
        final AtomicLong previousEventNumber = batchProcessDetails.getPreviousEventNumber();

        final List<PublishedEvent> publishedEvents = new ArrayList<>();
        try (final Stream<Event> eventStream = eventJdbcRepository.findAllFromEventNumberUptoPageSize(currentEventNumber.get(), PAGE_SIZE)) {

            eventStream
                    .peek(event -> currentEventNumber.set(event.getEventNumber().get()))
                    .forEach(event -> publishedEventInserter
                            .convertAndSave(event, previousEventNumber, activeStreamIds)
                            .ifPresent(publishedEvents::add));

        }

        final BatchProcessDetails currentBatchProcessDetails = batchProcessingDetailsCalculator.calculateNextBatchProcessDetails(
                batchProcessDetails,
                currentEventNumber,
                previousEventNumber,
                publishedEvents);

        if (currentBatchProcessDetails.getProcessedInBatchCount() > 0) {
            logger.info(format("Inserted %d PublishedEvents", currentBatchProcessDetails.getProcessCount()));
        } else {
            logger.info("Skipping inactive events...");
        }

        return currentBatchProcessDetails;
    }
}
