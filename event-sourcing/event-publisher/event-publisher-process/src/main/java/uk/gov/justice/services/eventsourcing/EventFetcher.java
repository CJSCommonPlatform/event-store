package uk.gov.justice.services.eventsourcing;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.sql.DataSource;

public class EventFetcher {

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Inject
    EventFetcherRepository eventFetcherRepository;

    /**
     * Method that gets an event from the event_log table by id.
     *
     * @return Optional<Event>
     */
    public Optional<Event> getEvent(final UUID id) {

        final DataSource eventStoreDataSource = eventStoreDataSourceProvider.getDefaultDataSource();
        try (final Connection connection = eventStoreDataSource.getConnection()) {
            return eventFetcherRepository.getEvent(id, connection);
        } catch (final SQLException e) {
            throw new EventFetchingException(format("Failed to get Event with id '%s'", id), e);
        }
    }

    /**
     * Method that gets an PublishedEvent from the published_event table by id.
     *
     * @return Optional<Event>
     */
    public Optional<PublishedEvent> getPublishedEvent(final UUID id) {

        final DataSource eventStoreDataSource = eventStoreDataSourceProvider.getDefaultDataSource();
        try (final Connection connection = eventStoreDataSource.getConnection()) {
            return eventFetcherRepository.getPublishedEvent(id, connection);
        } catch (final SQLException e) {
            throw new EventFetchingException(format("Failed to get PublishedEvent with id '%s'", id), e);
        }
    }
}
