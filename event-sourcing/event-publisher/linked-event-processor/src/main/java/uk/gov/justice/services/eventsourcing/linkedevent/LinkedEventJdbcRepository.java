package uk.gov.justice.services.eventsourcing.linkedevent;

import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.eventsourcing.prepublish.MissingEventNumberException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LinkedEventJdbcRepository {
    private static final String TRUNCATE_LINKED_EVENT = "TRUNCATE linked_event";
    private static final String TRUNCATE_PREPUBLISH_QUEUE = "TRUNCATE pre_publish_queue";

    private static final String INSERT_INTO_LINKED_EVENT_SQL = "INSERT into linked_event (" +
            "id, stream_id, position_in_stream, name, payload, metadata, date_created, event_number, previous_event_number) " +
            "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?)";


    public long truncate(final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(TRUNCATE_LINKED_EVENT)) {
            preparedStatement.executeUpdate();
        }
        try (final PreparedStatement preparedStatement = connection.prepareStatement(TRUNCATE_PREPUBLISH_QUEUE)) {
            preparedStatement.executeUpdate();
        }
        return 0;
    }

    public void insertLinkedEvent(final LinkedEvent linkedEvent, final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_LINKED_EVENT_SQL)) {
            preparedStatement.setObject(1, linkedEvent.getId());
            preparedStatement.setObject(2, linkedEvent.getStreamId());
            preparedStatement.setLong(3, linkedEvent.getSequenceId());
            preparedStatement.setString(4, linkedEvent.getName());
            preparedStatement.setString(5, linkedEvent.getPayload());
            preparedStatement.setString(6, linkedEvent.getMetadata());
            preparedStatement.setObject(7, toSqlTimestamp(linkedEvent.getCreatedAt()));
            preparedStatement.setLong(8, linkedEvent.getEventNumber().orElseThrow(() -> new MissingEventNumberException("Event with id '%s' does not have an event number")));
            preparedStatement.setLong(9, linkedEvent.getPreviousEventNumber());

            preparedStatement.execute();
        }
    }


}