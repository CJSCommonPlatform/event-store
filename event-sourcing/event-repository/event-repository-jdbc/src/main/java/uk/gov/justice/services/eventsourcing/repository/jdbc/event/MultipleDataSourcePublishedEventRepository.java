package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.sql.DataSource;

public class MultipleDataSourcePublishedEventRepository {

    private static final String SQL_FIND_ALL_SINCE = "SELECT * FROM published_event WHERE event_number > ? ORDER BY event_number ASC";
    private static final String SQL_FIND_RANGE = "SELECT * FROM published_event WHERE event_number >= ? AND event_number < ? ORDER BY event_number ASC";
    private static final String SQL_FIND_BY_ID = "SELECT * FROM published_event WHERE id = ?";
    private static final String SQL_FIND_LATEST_PUBLISHED_EVENT =
            "SELECT id, stream_id, position_in_stream, name, payload, metadata, date_created, event_number, previous_event_number " +
                    "FROM published_event " +
                    "ORDER BY event_number DESC " +
                    "LIMIT 1";

    private static final String ID = "id";
    private static final String STREAM_ID = "stream_id";
    private static final String POSITION_IN_STREAM = "position_in_stream";
    private static final String NAME = "name";
    private static final String METADATA = "metadata";
    private static final String PAYLOAD = "payload";
    private static final String DATE_CREATED = "date_created";
    private static final String EVENT_NUMBER = "event_number";
    private static final String PREVIOUS_EVENT_NUMBER = "previous_event_number";

    private final JdbcResultSetStreamer jdbcResultSetStreamer;
    private final PreparedStatementWrapperFactory preparedStatementWrapperFactory;
    private final DataSource dataSource;

    public MultipleDataSourcePublishedEventRepository(
            final JdbcResultSetStreamer jdbcResultSetStreamer,
            final PreparedStatementWrapperFactory preparedStatementWrapperFactory,
            final DataSource dataSource) {
        this.jdbcResultSetStreamer = jdbcResultSetStreamer;
        this.preparedStatementWrapperFactory = preparedStatementWrapperFactory;
        this.dataSource = dataSource;
    }

    /**
     * Returns a Stream of PublishedEvent of all events since eventNumber.
     *
     * @param eventNumber - exclusive start of events to return
     * @return a Stream of PublishedEvent
     */
    public Stream<PublishedEvent> findEventsSince(final long eventNumber) {

        try {
            final PreparedStatementWrapper psWrapper = preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_ALL_SINCE);

            psWrapper.setLong(1, eventNumber);

            return jdbcResultSetStreamer.streamOf(psWrapper, asPublishedEvent());
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(format("Failed to find events since event_number %d", eventNumber), e);
        }
    }

    /**
     * Returns a Stream of PublishedEvent for a given range of events numbers.
     *
     * @param fromEventNumber - inclusive start of range of event numbers
     * @param toEventNumber   - exclusive end of range of event numbers
     * @return a Stream of PublishedEvent
     */
    public Stream<PublishedEvent> findEventRange(final long fromEventNumber, final long toEventNumber) {

        try {
            final PreparedStatementWrapper psWrapper = preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_RANGE);

            psWrapper.setLong(1, fromEventNumber);
            psWrapper.setLong(2, toEventNumber);

            return jdbcResultSetStreamer.streamOf(psWrapper, asPublishedEvent());
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(format("Failed to find events from event_number %d to %d", fromEventNumber, toEventNumber), e);
        }
    }

    /**
     * Returns Optional of PublishedEvent for a given event id.
     *
     * @param eventId - id of the event to fetch
     * @return Optional of PublishedEvent
     */
    public Optional<PublishedEvent> findByEventId(final UUID eventId) {

        try {
            final PreparedStatementWrapper psWrapper = preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_BY_ID);

            psWrapper.setObject(1, eventId);

            final ResultSet resultSet = psWrapper.executeQuery();

            return resultSet.next()
                    ? of(asPublishedEvent().apply(resultSet))
                    : empty();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(format("Failed to find event with id %s", eventId), e);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public Optional<PublishedEvent> getLatestPublishedEvent() {
        try {
            try (final Connection connection = dataSource.getConnection();
                 final PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_LATEST_PUBLISHED_EVENT)) {

                try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                    if (resultSet.next()) {
                        final UUID id = fromString(resultSet.getString("id"));
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
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to get latest PublishedEvent", e);
        }
    }

    private Function<ResultSet, PublishedEvent> asPublishedEvent() {
        return resultSet -> {
            try {
                return new PublishedEvent((UUID) resultSet.getObject(ID),
                        (UUID) resultSet.getObject(STREAM_ID),
                        resultSet.getLong(POSITION_IN_STREAM),
                        resultSet.getString(NAME),
                        resultSet.getString(METADATA),
                        resultSet.getString(PAYLOAD),
                        fromSqlTimestamp(resultSet.getTimestamp(DATE_CREATED)),
                        resultSet.getLong(EVENT_NUMBER),
                        resultSet.getLong(PREVIOUS_EVENT_NUMBER)
                );
            } catch (final SQLException e) {
                throw new JdbcRepositoryException(e);
            }
        };
    }
}
