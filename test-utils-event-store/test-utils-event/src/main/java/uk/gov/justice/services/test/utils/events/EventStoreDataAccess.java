package uk.gov.justice.services.test.utils.events;

import static java.lang.String.format;
import static java.util.Optional.of;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.jdbc.persistence.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

public class EventStoreDataAccess {

    private final DataSource eventStoreDataSource;

    private static final String INSERT_INTO_EVENT_LOG_QUERY =
            "INSERT INTO event_log (" +
                    "id, stream_id, position_in_stream, name, payload, metadata, date_created" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_INTO_PUBLISHED_EVENT_QUERY =
            "INSERT INTO published_event (" +
                    "id, stream_id, position_in_stream, name, payload, metadata, date_created, event_number, previous_event_number" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_ALL_EVENTS_QUERY = "SELECT * FROM event_log";
    private static final String FIND_ALL_EVENTS_BY_STREAM_ID_QUERY = "SELECT * FROM event_log WHERE stream_id = ?";
    private static final String FIND_ALL_PUBLISHED_EVENTS_QUERY = "SELECT * FROM published_event";
    private static final String FIND_ALL_PUBLISHED_EVENTS_ORDERED_BT_EVENT_NUMBER_QUERY = "SELECT * FROM published_event ORDER BY event_number";

    public EventStoreDataAccess(final DataSource eventStoreDataSource) {
        this.eventStoreDataSource = eventStoreDataSource;
    }

    public void insertIntoEventLog(final Event event) {
        insertIntoEventLog(
                event.getId(),
                event.getStreamId(),
                event.getPositionInStream(),
                event.getCreatedAt(),
                event.getName(),
                event.getPayload(),
                event.getMetadata()
        );
    }

    public void insertIntoEventLog(
            final UUID eventLogId,
            final UUID streamId,
            final long positionInStream,
            final ZonedDateTime now,
            final String eventName,
            final String payload,
            final String metadata
    ) {

        final String query = INSERT_INTO_EVENT_LOG_QUERY;

        try (final Connection connection = eventStoreDataSource.getConnection()) {

            try (final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setObject(1, eventLogId);
                preparedStatement.setObject(2, streamId);
                preparedStatement.setLong(3, positionInStream);
                preparedStatement.setString(4, eventName);
                preparedStatement.setString(5, payload);
                preparedStatement.setString(6, metadata);
                preparedStatement.setObject(7, toSqlTimestamp(now));

                preparedStatement.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new DataAccessException(format("Failed to execute query '%s'", query), e);
        }
    }

    public void insertIntoPublishedEvent(final PublishedEvent publishedEvent) {

        final String query = INSERT_INTO_PUBLISHED_EVENT_QUERY;

        try (final Connection connection = eventStoreDataSource.getConnection()) {

            try (final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setObject(1, publishedEvent.getId());
                preparedStatement.setObject(2, publishedEvent.getStreamId());
                preparedStatement.setLong(3, publishedEvent.getPositionInStream());
                preparedStatement.setString(4, publishedEvent.getName());
                preparedStatement.setString(5, publishedEvent.getPayload());
                preparedStatement.setString(6, publishedEvent.getMetadata());
                preparedStatement.setObject(7, toSqlTimestamp(publishedEvent.getCreatedAt()));
                preparedStatement.setLong(8, publishedEvent.getEventNumber().get());
                preparedStatement.setLong(9, publishedEvent.getPreviousEventNumber());

                preparedStatement.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new DataAccessException(format("Failed to execute query '%s'", query), e);
        }
    }

    public List<Event> findAllEvents() {

        final String query = FIND_ALL_EVENTS_QUERY;

        final List<Event> events = new ArrayList<>();
        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {

                final Event event = new Event((UUID) resultSet.getObject("id"),
                        (UUID) resultSet.getObject("stream_id"),
                        resultSet.getLong("position_in_stream"),
                        resultSet.getString("name"),
                        resultSet.getString("metadata"),
                        resultSet.getString("payload"),
                        fromSqlTimestamp(resultSet.getTimestamp("date_created")),
                        of(resultSet.getLong("event_number")));

                events.add(event);
            }

            return events;
        } catch (final SQLException e) {
            throw new DataAccessException(format("Failed to execute query '%s'", query), e);
        }
    }

    public List<Event> findEventsByStream(final UUID streamId) {

        final String query = FIND_ALL_EVENTS_BY_STREAM_ID_QUERY;

        final List<Event> events = new ArrayList<>();
        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setObject(1, streamId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {

                    final Event event = new Event((UUID) resultSet.getObject("id"),
                            (UUID) resultSet.getObject("stream_id"),
                            resultSet.getLong("position_in_stream"),
                            resultSet.getString("name"),
                            resultSet.getString("metadata"),
                            resultSet.getString("payload"),
                            fromSqlTimestamp(resultSet.getTimestamp("date_created")),
                            of(resultSet.getLong("event_number")));

                    events.add(event);
                }
            }

            return events;
        } catch (final SQLException e) {
            throw new DataAccessException(format("Failed to execute query '%s'", query), e);
        }
    }

    public List<PublishedEvent> findAllPublishedEvents() {
        return doGetPublishedEvents(FIND_ALL_PUBLISHED_EVENTS_QUERY);
    }

    public List<PublishedEvent> findAllPublishedEventsOrderedByEventNumber() {
        return doGetPublishedEvents(FIND_ALL_PUBLISHED_EVENTS_ORDERED_BT_EVENT_NUMBER_QUERY);
    }

    private List<PublishedEvent> doGetPublishedEvents(final String query) {
        final List<PublishedEvent> events = new ArrayList<>();

        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {

                final PublishedEvent publishedEvent = new PublishedEvent(
                        (UUID) resultSet.getObject("id"),
                        (UUID) resultSet.getObject("stream_id"),
                        resultSet.getLong("position_in_stream"),
                        resultSet.getString("name"),
                        resultSet.getString("metadata"),
                        resultSet.getString("payload"),
                        fromSqlTimestamp(resultSet.getTimestamp("date_created")),
                        resultSet.getLong("event_number"),
                        resultSet.getLong("previous_event_number")
                );

                events.add(publishedEvent);
            }

            return events;
        } catch (final SQLException e) {
            throw new DataAccessException(format("Failed to execute query '%s'", query), e);
        }
    }
}
