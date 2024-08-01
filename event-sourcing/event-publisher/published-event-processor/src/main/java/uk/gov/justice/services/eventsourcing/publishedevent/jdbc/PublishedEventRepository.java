package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.PublishedEventException;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.sql.DataSource;

public class PublishedEventRepository {

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Inject
    private PublishedEventQueries publishedEventQueries;

    /**
     * Method that gets an PublishedEvent from the published_event table by id.
     *
     * @return Optional<Event>
     */
    public Optional<PublishedEvent> getPublishedEvent(final UUID id) {

        final DataSource eventStoreDataSource = eventStoreDataSourceProvider.getDefaultDataSource();
        try {
            return publishedEventQueries.getPublishedEvent(id, eventStoreDataSource);
        } catch (final SQLException e) {
            throw new PublishedEventException(format("Failed to get PublishedEvent with id '%s'", id), e);
        }
    }

    public void save(final PublishedEvent publishedEvent) {
        final DataSource defaultDataSource = eventStoreDataSourceProvider.getDefaultDataSource();
        try {
            publishedEventQueries.insertPublishedEvent(publishedEvent, defaultDataSource);
        } catch (final SQLException e) {
            throw new PublishedEventException(format("Unable to insert PublishedEvent with id '%s'", publishedEvent.getId()), e);
        }
    }
}
