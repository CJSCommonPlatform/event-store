package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration.helpers;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.events.EventBuilder.eventBuilder;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.jdbc.persistence.DataAccessException;
import uk.gov.justice.services.test.utils.events.EventStoreDataAccess;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

public class EventInserter {

    private final PositionInStreamCounter positionInStreamCounter = new PositionInStreamCounter();
    private final Random random = new Random();

    private final EventStoreDataAccess eventStoreDataAccess;

    public EventInserter(final DataSource eventStoreDataSource) {
        this.eventStoreDataAccess = new EventStoreDataAccess(eventStoreDataSource);
    }

    public void insertSomeEvents(final int numberOfEvents, final List<UUID> streamIds) {

        final int numberOfStreams = streamIds.size();
        try {
            for (int count = 0; count < numberOfEvents; count++) {

                final UUID streamId = streamIds.get(count % numberOfStreams);
                final Event event = eventBuilder()
                        .withId(randomUUID())
                        .withStreamId(streamId)
                        .withName("event " + (count + 1))
                        .withPositionInStream(positionInStreamCounter.getNextPosition(streamId))
                        .build();

                eventStoreDataAccess.insertIntoEventLog(event);

                if ((count % 100) == 0 && count != 0) {
                    System.out.println("Inserted " + count + " events...");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert event", e);
        }
    }
}
