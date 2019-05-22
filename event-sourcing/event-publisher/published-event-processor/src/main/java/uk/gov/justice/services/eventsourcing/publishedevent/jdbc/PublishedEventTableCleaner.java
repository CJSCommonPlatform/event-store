package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.publishedevent.PublishedEventException;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

public class PublishedEventTableCleaner {

    private static final String TABLE_NAME = "published_event";
    
    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Inject
    private DatabaseTableTruncator databaseTableTruncator;

    public void deleteAll() {

        final DataSource defaultDataSource = eventStoreDataSourceProvider.getDefaultDataSource();

        try {
            databaseTableTruncator.truncate(TABLE_NAME, defaultDataSource);
        } catch (final SQLException e) {
            throw new PublishedEventException(format("Failed to truncate table '%s'", TABLE_NAME), e);
        }
    }
}
