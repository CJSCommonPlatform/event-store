package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

public class EventNumberRenumberer {

    private static final String ALTER_SEQUENCE_QUERY = "ALTER SEQUENCE event_sequence_seq RESTART WITH 1";
    private static final String SELECT_QUERY = "SELECT id FROM event_log ORDER BY date_created";
    private static final String UPDATE_QUERY = "UPDATE event_log SET event_number = nextval('event_sequence_seq') WHERE id = ?";

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Inject
    private Logger logger;

    @Transactional(REQUIRED)
    public void renumberEventLogEventNumber() {
        resetSequence();
        renumberEvents();
    }

    private void renumberEvents() {

        logger.info("Renumbering events in the event_log table...");

        int count = 0;
        try (final Connection connection = eventStoreDataSourceProvider.getDefaultDataSource().getConnection();
             final PreparedStatement selectPreparedStatement = connection.prepareStatement(SELECT_QUERY);
             final PreparedStatement updatePreparedStatement = connection.prepareStatement(UPDATE_QUERY);
             final ResultSet resultSet = selectPreparedStatement.executeQuery()) {

            while (resultSet.next()) {
                count++;
                final UUID eventId = (UUID) resultSet.getObject(1);

                updatePreparedStatement.setObject(1, eventId);
                updatePreparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RebuildException("Failed to renumber event_number in event_log table", e);
        }

        logger.info(format("%d events in the event_log table renumbered", count));
    }

    private void resetSequence() {

        try (final Connection connection = eventStoreDataSourceProvider.getDefaultDataSource().getConnection()) {

            try (final PreparedStatement preparedStatement = connection.prepareStatement(ALTER_SEQUENCE_QUERY)) {
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RebuildException(format("Failed run sql statement '%s", ALTER_SEQUENCE_QUERY), e);
        }
    }
}
