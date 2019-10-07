package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BatchProcessingDetailsCalculator {

    public BatchProcessDetails createFirstBatchProcessDetails() {

        return new BatchProcessDetails(
                new AtomicLong(0),
                new AtomicLong(0),
                0,
                0
        );
    }

    public BatchProcessDetails calculateNextBatchProcessDetails(
            final BatchProcessDetails currentBatchProcessDetails,
            final AtomicLong currentEventNumber,
            final AtomicLong previousEventNumber,
            final List<PublishedEvent> publishedEvents) {

        final int processedInBatchCount = publishedEvents.size();
        return new BatchProcessDetails(
                previousEventNumber,
                new AtomicLong(currentEventNumber.get()),
                currentBatchProcessDetails.getProcessCount() + processedInBatchCount,
                processedInBatchCount
        );
    }
}
