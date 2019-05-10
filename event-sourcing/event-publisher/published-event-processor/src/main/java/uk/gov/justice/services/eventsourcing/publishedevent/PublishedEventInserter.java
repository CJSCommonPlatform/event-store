package uk.gov.justice.services.eventsourcing.publishedevent;

import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MissingEventNumberException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PublishedEventInserter {
    private static final String TRUNCATE_LINKED_EVENT = "TRUNCATE published_event";
    private static final String TRUNCATE_PREPUBLISH_QUEUE = "TRUNCATE pre_publish_queue";

    private static final String INSERT_INTO_LINKED_EVENT_SQL = "INSERT into published_event (" +
            "id, stream_id, position_in_stream, name, payload, metadata, date_created, event_number, previous_event_number) " +
            "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?)";


    public void truncate(final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(TRUNCATE_LINKED_EVENT)) {
            preparedStatement.executeUpdate();
        }
        try (final PreparedStatement preparedStatement = connection.prepareStatement(TRUNCATE_PREPUBLISH_QUEUE)) {
            preparedStatement.executeUpdate();
        }
    }

    public void insertPublishedEvent(final PublishedEvent publishedEvent, final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_LINKED_EVENT_SQL)) {
            preparedStatement.setObject(1, publishedEvent.getId());
            preparedStatement.setObject(2, publishedEvent.getStreamId());
            preparedStatement.setLong(3, publishedEvent.getSequenceId());
            preparedStatement.setString(4, publishedEvent.getName());
            preparedStatement.setString(5, publishedEvent.getPayload());
            preparedStatement.setString(6, publishedEvent.getMetadata());
            preparedStatement.setObject(7, toSqlTimestamp(publishedEvent.getCreatedAt()));
            preparedStatement.setLong(8, publishedEvent.getEventNumber().orElseThrow(() -> new MissingEventNumberException("Event with id '%s' does not have an event number")));
            preparedStatement.setLong(9, publishedEvent.getPreviousEventNumber());

            preparedStatement.execute();
        }
    }
}
