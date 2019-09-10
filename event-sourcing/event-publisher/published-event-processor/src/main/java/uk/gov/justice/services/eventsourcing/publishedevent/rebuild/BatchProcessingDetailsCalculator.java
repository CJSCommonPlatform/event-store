package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MissingEventNumberException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BatchProcessingDetailsCalculator {

    public BatchProcessDetails createFirstBatchProcessDetails() {

        return new BatchProcessDetails(
                new AtomicLong(0),
                new AtomicLong(0),
                0,
                false
        );
    }

    public BatchProcessDetails calculateNextBatchProcessDetails(
            final BatchProcessDetails currentBatchProcessDetails,
            final AtomicLong previousEventNumber,
            final List<PublishedEvent> publishedEvents) {

        if (publishedEvents.isEmpty()) {
            return new BatchProcessDetails(
                    previousEventNumber,
                    currentBatchProcessDetails.getCurrentEventNumber(),
                    currentBatchProcessDetails.getProcessCount(),
                    true
            );
        }

        final PublishedEvent lastPublishedEvent = publishedEvents.get(publishedEvents.size() - 1);

        final Long newCurrentEventNumber = lastPublishedEvent.getEventNumber().orElseThrow(() -> new MissingEventNumberException(""));
        return new BatchProcessDetails(
                previousEventNumber,
                new AtomicLong(newCurrentEventNumber),
                currentBatchProcessDetails.getProcessCount() + publishedEvents.size(),
                false
        );
    }
}
