package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;
import static uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventStatements.INSERT_INTO_PUBLISHED_EVENT_SQL;
import static uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventStatements.SELECT_FROM_PUBLISHED_EVENT_QUERY;
import static uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventStatements.TRUNCATE_PREPUBLISH_QUEUE;
import static uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventStatements.TRUNCATE_PUBLISHED_EVENT;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MissingEventNumberException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

public class PublishedEventQueries {

    public void truncate(final DataSource dataSource) throws SQLException {

        try (final Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(TRUNCATE_PUBLISHED_EVENT)) {
                preparedStatement.executeUpdate();
            }
            try (final PreparedStatement preparedStatement = connection.prepareStatement(TRUNCATE_PREPUBLISH_QUEUE)) {
                preparedStatement.executeUpdate();
            }
        }
    }

    public void insertPublishedEvent(final PublishedEvent publishedEvent, final DataSource dataSource) throws SQLException {

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_PUBLISHED_EVENT_SQL)) {
            preparedStatement.setObject(1, publishedEvent.getId());
            preparedStatement.setObject(2, publishedEvent.getStreamId());
            preparedStatement.setLong(3, publishedEvent.getPositionInStream());
            preparedStatement.setString(4, publishedEvent.getName());
            preparedStatement.setString(5, publishedEvent.getPayload());
            preparedStatement.setString(6, publishedEvent.getMetadata());
            preparedStatement.setObject(7, toSqlTimestamp(publishedEvent.getCreatedAt()));
            preparedStatement.setLong(8, publishedEvent.getEventNumber().orElseThrow(() -> new MissingEventNumberException("Event with id '%s' does not have an event number")));
            preparedStatement.setLong(9, publishedEvent.getPreviousEventNumber());

            preparedStatement.execute();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public Optional<PublishedEvent> getPublishedEvent(final UUID id, final DataSource dataSource) throws SQLException {

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FROM_PUBLISHED_EVENT_QUERY)) {

            preparedStatement.setObject(1, id);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    final UUID streamId = fromString(resultSet.getString("stream_id"));
                    final Long positionInStream = resultSet.getLong("position_in_stream");
                    final String name = resultSet.getString("name");
                    final String metadata = resultSet.getString("metadata");
                    final String payload = resultSet.getString("payload");
                    final ZonedDateTime createdAt = fromSqlTimestamp(resultSet.getTimestamp("date_created"));
                    final long eventNumber = resultSet.getLong("event_number");
                    final long previousEventNumber = resultSet.getLong("previous_event_number");

                    return of(new PublishedEvent(
                            id,
                            streamId,
                            positionInStream,
                            name,
                            metadata,
                            payload,
                            createdAt,
                            eventNumber,
                            previousEventNumber)
                    );
                }
            }
        }

        return empty();
    }
}
