package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.RebuildException;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class EventNumberSequenceResetter {

    private static final String ALTER_SEQUENCE_QUERY = "ALTER SEQUENCE event_sequence_seq RESTART WITH 1";

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Transactional(REQUIRES_NEW)
    public void resetSequence() {

        try (final Connection connection = eventStoreDataSourceProvider.getDefaultDataSource().getConnection()) {

            try (final PreparedStatement preparedStatement = connection.prepareStatement(ALTER_SEQUENCE_QUERY)) {
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RebuildException(format("Failed run sql statement '%s", ALTER_SEQUENCE_QUERY), e);
        }
    }
}
