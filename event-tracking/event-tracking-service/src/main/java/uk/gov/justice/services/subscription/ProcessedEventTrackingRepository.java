package uk.gov.justice.services.subscription;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

public class ProcessedEventTrackingRepository {

    private static final String INSERT_SQL =
            "INSERT INTO processed_event (event_number, previous_event_number, source, component) " +
                    "VALUES (?, ?, ?, ?)";

    private static final String SELECT_MAX_SQL =
            "SELECT event_number, previous_event_number, source, component " +
                    "FROM processed_event " +
                    "WHERE source = ? " +
                    "AND component = ? " +
                    "ORDER BY event_number DESC LIMIT 1";

    private static final String SELECT_ALL_DESCENDING_ORDER_SQL =
            "SELECT event_number, previous_event_number " +
                    "FROM processed_event " +
                    "WHERE source = ? " +
                    "AND component = ? " +
                    "ORDER BY event_number DESC";

    @Inject
    private JdbcResultSetStreamer jdbcResultSetStreamer;

    @Inject
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @Inject
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    public void save(final ProcessedEventTrackItem processedEventTrackItem) {

        try (
                final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {

            preparedStatement.setLong(1, processedEventTrackItem.getEventNumber());
            preparedStatement.setLong(2, processedEventTrackItem.getPreviousEventNumber());
            preparedStatement.setString(3, processedEventTrackItem.getSource());
            preparedStatement.setString(4, processedEventTrackItem.getComponentName());

            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new ProcessedEventTrackingException("Failed to insert ProcessedEvent into viewstore", e);
        }
    }

    public Stream<ProcessedEventTrackItem> getAllProcessedEventsDescendingOrder(final String source, final String componentName) {

        try {
            final PreparedStatementWrapper preparedStatement = preparedStatementWrapperFactory.preparedStatementWrapperOf(
                    viewStoreJdbcDataSourceProvider.getDataSource(), SELECT_ALL_DESCENDING_ORDER_SQL);

            preparedStatement.setString(1, source);
            preparedStatement.setString(2, componentName);

            return jdbcResultSetStreamer.streamOf(preparedStatement, resultSet -> {

                try {
                    final long eventNumber = resultSet.getLong("event_number");
                    final long previousEventNumber = resultSet.getLong("previous_event_number");
                    return new ProcessedEventTrackItem(previousEventNumber, eventNumber, source, componentName);
                } catch (final SQLException e) {
                    throw new ProcessedEventTrackingException("Failed to get row from processed_event table", e);
                }
            });

        } catch (final SQLException e) {
            throw new ProcessedEventTrackingException("Failed to get processed events from processed_event table", e);
        }
    }

    public Optional<ProcessedEventTrackItem> getLatestProcessedEvent(final String source, final String componentName) {

        try (
                final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_MAX_SQL)) {

            preparedStatement.setString(1, source);
            preparedStatement.setString(2, componentName);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final long eventNumber = resultSet.getLong("event_number");
                    final long previousEventNumber = resultSet.getLong("previous_event_number");

                    return of(new ProcessedEventTrackItem(previousEventNumber, eventNumber, source, componentName));
                }

                return empty();
            }
        } catch (final SQLException e) {
            throw new ProcessedEventTrackingException("Failed to insert ProcessedEvent into viewstore", e);
        }
    }
}
