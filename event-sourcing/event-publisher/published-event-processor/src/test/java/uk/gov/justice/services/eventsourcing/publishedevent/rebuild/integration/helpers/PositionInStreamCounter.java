package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PositionInStreamCounter {

    private final Map<UUID, Long> cache = new HashMap<>();

    public long getNextPosition(final UUID streamId) {

        final Long position = cache.computeIfAbsent(streamId, uuid -> 1L);

        cache.put(streamId, position + 1);

        return position;
    }
}
