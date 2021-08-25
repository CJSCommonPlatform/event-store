package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static uk.gov.justice.services.eventsourcing.repository.jdbc.PublishQueue.PublishQueueTableName.PRE_PUBLISH_QUEUE_TABLE;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

public class PrePublishQueueRepository implements PublishQueue {

    @Inject
    private PublishQueuesDataAccess publishQueuesDataAccess;

    @Override
    public void addToQueue(final UUID eventId, final ZonedDateTime queuedAt) {
        publishQueuesDataAccess.addToQueue(eventId, queuedAt, PRE_PUBLISH_QUEUE_TABLE);
    }

    @Override
    public Optional<UUID> popNextEventId() {
        return publishQueuesDataAccess.popNextEventId(PRE_PUBLISH_QUEUE_TABLE);
    }

    @Override
    public int getSizeOfQueue() {
        return publishQueuesDataAccess.getSizeOfQueue(PRE_PUBLISH_QUEUE_TABLE);
    }
}
