package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;

import java.util.Set;
import java.util.UUID;

public class ActiveEventFilter {

    public boolean isActiveEvent(final Event event, final Set<UUID> activeStreamIds) {
        return activeStreamIds.contains(event.getStreamId());
    }
}
