package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;
import static uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventStatements.INSERT_INTO_PUBLISHED_EVENT_SQL;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MissingEventNumberException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.util.io.Closer;
import uk.gov.justice.services.jdbc.persistence.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

public class BatchedPublishedEventInserter implements AutoCloseable {

    private final Closer closer;

    private PreparedStatement preparedStatement;
    private Connection connection;

    public BatchedPublishedEventInserter(final Closer closer) {
        this.closer = closer;
    }

    public void prepareForInserts(final DataSource eventStoreDataSource) {
        try {
            connection = eventStoreDataSource.getConnection();
            preparedStatement = connection.prepareStatement(INSERT_INTO_PUBLISHED_EVENT_SQL);
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to prepare statement for batch insert of PublishedEvents", e);
        }
    }

    public PublishedEvent addToBatch(final PublishedEvent publishedEvent) {

        try {
            preparedStatement.setObject(1, publishedEvent.getId());
            preparedStatement.setObject(2, publishedEvent.getStreamId());
            preparedStatement.setLong(3, publishedEvent.getPositionInStream());
            preparedStatement.setString(4, publishedEvent.getName());
            preparedStatement.setString(5, publishedEvent.getPayload());
            preparedStatement.setString(6, publishedEvent.getMetadata());
            preparedStatement.setObject(7, toSqlTimestamp(publishedEvent.getCreatedAt()));
            preparedStatement.setLong(8, publishedEvent.getEventNumber().orElseThrow(() -> new MissingEventNumberException("Event with id '%s' does not have an event number")));
            preparedStatement.setLong(9, publishedEvent.getPreviousEventNumber());

            preparedStatement.addBatch();

            return publishedEvent;

        } catch (final SQLException e) {
            throw new DataAccessException("Failed to add PublishedEvent to batch", e);
        }
    }

    public void insertBatch() {

        try {
            preparedStatement.executeBatch();
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to insert batch of PublishedEvents", e);
        }
    }

    @Override
    public void close() {
       closer.closeQuietly(preparedStatement);
       closer.closeQuietly(connection);
    }
}
