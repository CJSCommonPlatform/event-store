package uk.gov.justice.services.eventsourcing;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class EventFetcherRepository {

    private static final String SELECT_FROM_EVENT_LOG_QUERY =
            "SELECT stream_id, position_in_stream, name, payload, metadata, date_created " +
                    "FROM event_log " +
                    "WHERE id = ?";

    private static final String SELECT_FROM_LINKED_EVENT_QUERY =
            "SELECT stream_id, position_in_stream, name, payload, metadata, date_created, event_number, previous_event_number " +
                    "FROM linked_event " +
                    "WHERE id = ?";

    /**
     * Method that gets an event from the event_log table by id.
     *
     * @return Optional<Event>
     */
    public Optional<Event> getEvent(final UUID id, final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FROM_EVENT_LOG_QUERY)) {

            preparedStatement.setObject(1, id);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    final UUID streamId = fromString(resultSet.getString("stream_id"));
                    final Long positionInStream = resultSet.getLong("position_in_stream");
                    final String name = resultSet.getString("name");
                    final String metadata = resultSet.getString("metadata");
                    final String payload = resultSet.getString("payload");
                    final ZonedDateTime createdAt = fromSqlTimestamp(resultSet.getTimestamp("date_created"));

                    return of(new Event(
                            id,
                            streamId,
                            positionInStream,
                            name,
                            metadata,
                            payload,
                            createdAt)
                    );
                }
            }
        } 

        return empty();
    }

    public Optional<LinkedEvent> getLinkedEvent(final UUID id, final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FROM_LINKED_EVENT_QUERY)) {

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

                    return of(new LinkedEvent(
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
