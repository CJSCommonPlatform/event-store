package uk.gov.justice.services.eventsourcing.publishing;

import static javax.transaction.Transactional.TxType.MANDATORY;

import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.time.StopWatch;

public class SequenceNumberUpdater {

    private static final long HEAD = 0;
    private static final long TAIL = 0;

    private static final String LOCK_TABLE_SQL = "LOCK TABLE event_sequence IN ACCESS EXCLUSIVE MODE";

    private static final String NEXT_VAL_FROM_SEQUENCE_SQL = "SELECT nextval('event_sequence_number_seq')";

    private static final String SELECT_PREVIOUS_SEQUENCE_NUMBER_SQL = "SELECT sequence_number FROM event_sequence WHERE next = " + HEAD;
    private static final String UPDATE_NEXT_COLUMN_IN_PREVIOUS_ROW_SQL = "UPDATE event_sequence SET next = ? WHERE next = " + HEAD;
    private static final String INSERT_NEW_ROW_SQL = "INSERT INTO event_sequence (event_id, sequence_number, previous, next) VALUES (?, ?, ?, ?)";

    @Inject
    SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Transactional(MANDATORY)
    public long update(final UUID eventId) throws SQLException {

        try (final Connection connection = subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()) {

            connection.setAutoCommit(false);

            lockTable(connection);

            final long nextValFromPostgresSequence = nextValFromPostgresSequence(connection);

            final long previous = selectFromPreviousRow(connection);

            updateNextColumnInPreviousRow(nextValFromPostgresSequence, connection);
            insertRow(eventId, nextValFromPostgresSequence, previous, connection);

            connection.commit();

            return nextValFromPostgresSequence;
        }
    }

    private void lockTable(final Connection connection) throws SQLException {
        try(final PreparedStatement preparedStatement = connection.prepareStatement(LOCK_TABLE_SQL)) {
            preparedStatement.execute();
        }
    }

    private long nextValFromPostgresSequence(final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(NEXT_VAL_FROM_SEQUENCE_SQL)) {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private long selectFromPreviousRow(final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PREVIOUS_SEQUENCE_NUMBER_SQL)) {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        }

        return TAIL;
    }

    private void updateNextColumnInPreviousRow(final long nextValFromPostgresSequence, final Connection connection) throws SQLException {


        try (final PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_NEXT_COLUMN_IN_PREVIOUS_ROW_SQL)) {

            preparedStatement.setLong(1, nextValFromPostgresSequence);
            preparedStatement.executeUpdate();
        }
    }

    private void insertRow(final UUID eventId, final long nextValFromPostgresSequence, final long previous, final Connection connection) throws SQLException {


        try (final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_ROW_SQL)) {

            preparedStatement.setObject(1, eventId);
            preparedStatement.setLong(2, nextValFromPostgresSequence);
            preparedStatement.setLong(3, previous);
            preparedStatement.setLong(4, HEAD);

            preparedStatement.executeUpdate();
        }
    }
}
