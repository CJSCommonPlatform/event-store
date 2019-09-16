package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.RebuildException;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

public class BatchEventRenumberer {

    private static final String SELECT_QUERY = "SELECT id FROM event_log ORDER BY date_created";
    private static final String UPDATE_QUERY = "UPDATE event_log SET event_number = nextval('event_sequence_seq') WHERE id = ?";

    private static final int BATCH_SIZE = 1_000;

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Inject
    private EventIdsByBatchProvider eventIdsByBatchProvider;

    @Inject
    private Logger logger;

    @Transactional(REQUIRES_NEW)
    public int renumberEvents(final EventIdBatch eventIdBatch) {

        final List<UUID> eventIds = eventIdBatch.getEventIds();

        try (final Connection connection = eventStoreDataSourceProvider.getDefaultDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_QUERY)) {

            for (final UUID eventId : eventIds) {
                preparedStatement.setObject(1, eventId);
                preparedStatement.executeUpdate();
            }

            return eventIds.size();

        } catch (SQLException e) {
            throw new RebuildException("Failed to renumber event_number in event_log table", e);
        }
    }

    @Transactional(REQUIRES_NEW)
    public List<EventIdBatch> getEventIdsOrderedByCreationDate() {

        final List<EventIdBatch> eventIdBatches = new ArrayList<>();

        try (final Connection connection = eventStoreDataSourceProvider.getDefaultDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_QUERY);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                eventIdBatches.add(eventIdsByBatchProvider.getNextBatchOfIds(resultSet, BATCH_SIZE));
            }

            return eventIdBatches;

        } catch (SQLException e) {
            throw new RebuildException("Failed to get ids from event_log table", e);
        }
    }
}
