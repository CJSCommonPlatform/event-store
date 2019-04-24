package uk.gov.justice.services.eventsourcing.publishedevent;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.prepublish.PrePublishRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventInserter;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import javax.inject.Inject;

public class PublishedEventRepository {

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Inject
    private PublishedEventInserter publishedEventInserter;

    @Inject
    private PrePublishRepository prePublishRepository;

    public void save(final PublishedEvent publishedEvent) {
        try (final Connection connection = eventStoreDataSourceProvider.getDefaultDataSource().getConnection()) {
            publishedEventInserter.insertPublishedEvent(publishedEvent, connection);
        } catch (final SQLException e) {
            throw new PublishedEventSQLException(format("Unable to insert PublishedEvent with id '%s'", publishedEvent.getId()), e);
        }
    }

    public long getPreviousEventNumber(final UUID eventId, final long eventNumber) {

        try (final Connection connection = eventStoreDataSourceProvider.getDefaultDataSource().getConnection()) {
            return prePublishRepository.getPreviousEventNumber(eventNumber, connection);
        } catch (final SQLException e) {
            throw new PublishedEventSQLException(format("Unable to get previous event number for event with id '%s'", eventId), e);
        }
    }
}
