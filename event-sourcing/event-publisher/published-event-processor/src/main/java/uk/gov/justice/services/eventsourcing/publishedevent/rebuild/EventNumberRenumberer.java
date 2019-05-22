package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.inject.Inject;

public class EventNumberRenumberer {

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    public void renumberEventLogEventNumber() {

        try(final Connection connection = eventStoreDataSourceProvider.getDefaultDataSource().getConnection()) {

            try(final PreparedStatement preparedStatement = connection.prepareStatement("ALTER SEQUENCE event_sequence_seq RESTART WITH 1")) {
               preparedStatement.executeUpdate();
            }

            try(final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE event_log SET event_number = nextval('event_sequence_seq')")) {
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RebuildException("Failed to renumber event_number in event_log table", e);
        }
    }
}
