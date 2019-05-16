package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.publishedevent.PublishedEventException;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.SQLException;
import java.util.UUID;

import javax.inject.Inject;
import javax.sql.DataSource;

public class PreviousEventNumberFinder {

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Inject
    private PrePublishRepository prePublishRepository;

    public long getPreviousEventNumber(final UUID eventId, final long eventNumber) {

        final DataSource defaultDataSource = eventStoreDataSourceProvider.getDefaultDataSource();
        try {
            return prePublishRepository.getPreviousEventNumber(eventNumber, defaultDataSource);
        } catch (final SQLException e) {
            throw new PublishedEventException(format("Unable to get previous event number for event with id '%s'", eventId), e);
        }
    }
}
