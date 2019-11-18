package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.eventsourcing.util.io.Closer;

import javax.inject.Inject;

public class BatchedPublishedEventInserterFactory {

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Inject
    private Closer closer;

    public BatchedPublishedEventInserter createInitialised() {

        final BatchedPublishedEventInserter batchedPublishedEventInserter = new BatchedPublishedEventInserter(closer);

        batchedPublishedEventInserter.prepareForInserts(eventStoreDataSourceProvider.getDefaultDataSource());

        return batchedPublishedEventInserter;
    }
}
