package uk.gov.justice.services.eventsourcing;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static javax.transaction.Transactional.TxType.MANDATORY;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * The EventDeQueuer class provides a method that returns an event from the event_log.
 */
public class EventDeQueuer {

    public static final String PRE_PUBLISH_TABLE_NAME = "pre_publish_queue";
    public static final String PUBLISH_TABLE_NAME = "publish_queue";

    private static final String SELECT_FROM_PUBLISH_TABLE_QUERY_PATTERN = "SELECT id, event_log_id FROM %s ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED ";
    private static final String SELECT_FROM_EVENT_LOG_QUERY = "SELECT stream_id, sequence_id, name, payload, metadata, date_created " +
            "FROM event_log WHERE id = ?";
    private static final String DELETE_FROM_PUBLISH_TABLE_QUERY_PATTERN = "DELETE FROM %s where id = ?";

    @Inject
    SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    /**
     * Method that gets the next event to process by
     * querying the pre_publish_queue table for id & event_log_id,
     * deleting the entry from the publish queue using the id and
     * then gets the record from the event_log table using the event_log_id.
     *
     * @return Optional<Event>
     */
    @Transactional(MANDATORY)
    public Optional<Event> popNextEvent(final String tableName) {

        final String sql = format(SELECT_FROM_PUBLISH_TABLE_QUERY_PATTERN, tableName);
        try (final Connection connection = subscriptionDataSourceProvider.getEventStoreDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                final long publishQueueId = resultSet.getLong("id");
                final UUID eventLogId = fromString(resultSet.getString("event_log_id"));

                deletePublishQueueRow(publishQueueId, tableName, connection);

                return getEventFromEventLogTable(eventLogId, connection);
            }
        } catch (final SQLException e) {
            throw new PublishQueueException(format("Failed to publish from %s table", tableName), e);
        }

        return empty();
    }

    /**
     * Method that gets the next event from the event_log table using the event_log_id.
     *
     * @return Optional<Event>
     */
    private Optional<Event> getEventFromEventLogTable(final UUID eventLogId, final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FROM_EVENT_LOG_QUERY)) {

            preparedStatement.setObject(1, eventLogId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    final UUID streamId = fromString(resultSet.getString("stream_id"));
                    final Long sequenceId = resultSet.getLong("sequence_id");
                    final String name = resultSet.getString("name");
                    final String metadata = resultSet.getString("metadata");
                    final String payload = resultSet.getString("payload");
                    final ZonedDateTime createdAt = fromSqlTimestamp(resultSet.getTimestamp("date_created"));

                    return of(new Event(
                            eventLogId,
                            streamId,
                            sequenceId,
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

    /**
     * Method that deletes the next event from the pre_publish_queue table using the event_log_id.
     */
    private void deletePublishQueueRow(final long eventLogId, final String tableName, final Connection connection) throws SQLException {

        final String sql = format(DELETE_FROM_PUBLISH_TABLE_QUERY_PATTERN, tableName);
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, eventLogId);
            preparedStatement.executeUpdate();
        }
    }
}
