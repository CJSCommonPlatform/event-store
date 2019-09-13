package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventIdsByBatchProvider {

    /**
     * Gets the event ids in batches from the result set. Please note: it is assumed the
     * <code>result.next()</code> would have already been called on the result set
     * (hence the do..while block rather then a while). This means the first call to
     * resultSet.getObject(...) will always return the next id.
     *
     * @param resultSet The ResultSet of event ids. next() will always have been called
     *                  previous to this method being called
     * @param batchSize The size of the required batch
     * @return A List of event ids
     * @throws SQLException on failure
     */
    public EventIdBatch getNextBatchOfIds(final ResultSet resultSet, final int batchSize) throws SQLException {

        final List<UUID> eventIds = new ArrayList<>(batchSize);

        int count = 0;
        do {
            count++;
            final UUID eventId = (UUID) resultSet.getObject(1);
            eventIds.add(eventId);

            if (count % batchSize == 0) {
                break;
            }
        } while (resultSet.next());

        return new EventIdBatch(eventIds);
    }
}
