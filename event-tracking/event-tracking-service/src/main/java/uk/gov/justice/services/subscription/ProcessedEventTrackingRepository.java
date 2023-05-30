package uk.gov.justice.services.subscription;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.transaction.Transactional.TxType.REQUIRED;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class ProcessedEventTrackingRepository {

    private static final String INSERT_SQL =
            "INSERT INTO processed_event (event_id, event_number, previous_event_number, source, component) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_MAX_SQL =
            "SELECT event_id, event_number, previous_event_number, source, component " +
                    "FROM processed_event " +
                    "WHERE source = ? " +
                    "AND component = ? " +
                    "ORDER BY event_number DESC LIMIT 1";

    private static final String SELECT_ALL_DESCENDING_ORDER_SQL =
            "SELECT event_id, event_number, previous_event_number " +
                    "FROM processed_event " +
                    "WHERE source = ? " +
                    "AND component = ? " +
                    "ORDER BY event_number DESC";

    private static final String SELECT_LESS_THAN_EVENT_NUMBER_IN_DESCENDING_ORDER_SQL =
            "SELECT event_id, event_number, previous_event_number " +
                    "FROM processed_event " +
                    "WHERE source = ? " +
                    "AND component = ? " +
                    "AND event_number < ? " +
                    "ORDER BY event_number DESC " +
                    "LIMIT ?";

    @Inject
    private JdbcResultSetStreamer jdbcResultSetStreamer;

    @Inject
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @Inject
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @Transactional(REQUIRED)
    public void save(final ProcessedEvent processedEvent) {

        try (
                final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {

            preparedStatement.setObject(1, processedEvent.getEventId());
            preparedStatement.setLong(2, processedEvent.getEventNumber());
            preparedStatement.setLong(3, processedEvent.getPreviousEventNumber());
            preparedStatement.setString(4, processedEvent.getSource());
            preparedStatement.setString(5, processedEvent.getComponentName());

            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new ProcessedEventTrackingException("Failed to insert ProcessedEvent into viewstore", e);
        }
    }

    @Transactional(REQUIRED)
    public Stream<ProcessedEvent> getAllProcessedEventsDescendingOrder(final String source, final String componentName) {

        try {
            final PreparedStatementWrapper preparedStatement = preparedStatementWrapperFactory.preparedStatementWrapperOf(
                    viewStoreJdbcDataSourceProvider.getDataSource(), SELECT_ALL_DESCENDING_ORDER_SQL);

            preparedStatement.setString(1, source);
            preparedStatement.setString(2, componentName);

            return jdbcResultSetStreamer.streamOf(preparedStatement, resultSet -> {

                try {
                    final UUID eventId = (UUID) resultSet.getObject("event_id");
                    final long eventNumber = resultSet.getLong("event_number");
                    final long previousEventNumber = resultSet.getLong("previous_event_number");
                    return new ProcessedEvent(eventId, previousEventNumber, eventNumber, source, componentName);
                } catch (final SQLException e) {
                    throw new ProcessedEventTrackingException("Failed to get row from processed_event table", e);
                }
            });

        } catch (final SQLException e) {
            throw new ProcessedEventTrackingException("Failed to get processed events from processed_event table", e);
        }
    }

    @Transactional(REQUIRES_NEW)
    public List<ProcessedEvent> getProcessedEventsLessThanEventNumberInDescendingOrder(
            final Long fromEventNumber,
            final Long batchSize,
            final String source,
            final String componentName) {

        final ArrayList<ProcessedEvent> processedEvents = new ArrayList<>();

        try(final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LESS_THAN_EVENT_NUMBER_IN_DESCENDING_ORDER_SQL)) {

            preparedStatement.setString(1, source);
            preparedStatement.setString(2, componentName);
            preparedStatement.setLong(3, fromEventNumber);
            preparedStatement.setLong(4, batchSize);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    final UUID eventId = (UUID) resultSet.getObject("event_id");
                    final long eventNumber = resultSet.getLong("event_number");
                    final long previousEventNumber = resultSet.getLong("previous_event_number");
                    final ProcessedEvent processedEvent = new ProcessedEvent(
                            eventId,
                            previousEventNumber,
                            eventNumber,
                            source,
                            componentName);

                    processedEvents.add(processedEvent);
                }

            } catch (final SQLException e) {
                throw new ProcessedEventTrackingException("Failed to get row from processed_event table", e);
            }

        } catch (final SQLException e) {
            throw new ProcessedEventTrackingException("Failed to get processed events from processed_event table", e);
        }

        return processedEvents;
    }

    @Transactional(REQUIRED)
    public Optional<ProcessedEvent> getLatestProcessedEvent(final String source, final String componentName) {

        try (
                final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_MAX_SQL)) {

            preparedStatement.setString(1, source);
            preparedStatement.setString(2, componentName);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final UUID eventId = (UUID) resultSet.getObject("event_id");
                    final long eventNumber = resultSet.getLong("event_number");
                    final long previousEventNumber = resultSet.getLong("previous_event_number");

                    return of(new ProcessedEvent(eventId, previousEventNumber, eventNumber, source, componentName));
                }

                return empty();
            }
        } catch (final SQLException e) {
            throw new ProcessedEventTrackingException("Failed to insert ProcessedEvent into viewstore", e);
        }
    }
}
