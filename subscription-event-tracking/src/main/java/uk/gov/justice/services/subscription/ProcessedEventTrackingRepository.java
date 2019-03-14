package uk.gov.justice.services.subscription;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
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
            "INSERT INTO processed_event (event_number, previous_event_number, source) " +
                    "VALUES (?, ?, ?)";

    private static final String SELECT_MAX_SQL =
            "SELECT event_number, previous_event_number, source " +
                    "FROM processed_event " +
                    "WHERE source = ? " +
                    "ORDER BY event_number DESC LIMIT 1";

    private static final String SELECT_SQL =
            "SELECT event_number, previous_event_number " +
                    "FROM processed_event " +
                    "WHERE source = ? " +
                    "ORDER BY event_number ASC";

    @Inject
    JdbcRepositoryHelper jdbcRepositoryHelper;

    @Inject
    ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    public void save(final ProcessedEventTrackItem processedEventTrackItem) {

        try (
                final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {

            preparedStatement.setLong(1, processedEventTrackItem.getEventNumber());
            preparedStatement.setLong(2, processedEventTrackItem.getPreviousEventNumber());
            preparedStatement.setString(3, processedEventTrackItem.getSource());

            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new ProcessedEventTrackingException("Failed to insert ProcessedEvent into viewstore", e);
        }
    }

    public Stream<ProcessedEventTrackItem> getAllProcessedEvents(final String source) {

        try {
            final PreparedStatementWrapper preparedStatement = jdbcRepositoryHelper.preparedStatementWrapperOf(
                    viewStoreJdbcDataSourceProvider.getDataSource(), SELECT_SQL);

            preparedStatement.setString(1, source);

            return jdbcRepositoryHelper.streamOf(preparedStatement, resultSet -> {

                try {
                    final long eventNumber = resultSet.getLong("event_number");
                    final long previousEventNumber = resultSet.getLong("previous_event_number");
                    return new ProcessedEventTrackItem(previousEventNumber, eventNumber, source);
                } catch (final SQLException e) {
                    throw new ProcessedEventTrackingException("Failed to get row from processed_event table", e);
                }
            });

        } catch (final SQLException e) {
            throw new ProcessedEventTrackingException("Failed to get processed events from processed_event table", e);
        }
    }

    public Optional<ProcessedEventTrackItem> getLatestProcessedEvent(final String source) {

        try (
                final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_MAX_SQL)) {

            preparedStatement.setString(1, source);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final long eventNumber = resultSet.getLong("event_number");
                    final long previousEventNumber = resultSet.getLong("previous_event_number");

                    return of(new ProcessedEventTrackItem(previousEventNumber, eventNumber, source));
                }

                return empty();
            }
        } catch (final SQLException e) {
            throw new ProcessedEventTrackingException("Failed to insert ProcessedEvent into viewstore", e);
        }
    }
}
