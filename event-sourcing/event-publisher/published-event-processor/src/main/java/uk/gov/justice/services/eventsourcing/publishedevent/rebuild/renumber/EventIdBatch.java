package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber;

import java.util.List;
import java.util.UUID;

public class EventIdBatch {

    final List<UUID> eventIds;

    public EventIdBatch(final List<UUID> eventIds) {
        this.eventIds = eventIds;
    }

    public List<UUID> getEventIds() {
        return eventIds;
    }
}
