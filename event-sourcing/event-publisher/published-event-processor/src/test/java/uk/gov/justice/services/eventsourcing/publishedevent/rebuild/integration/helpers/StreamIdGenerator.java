package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration.helpers;

import static java.util.UUID.randomUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StreamIdGenerator {

    public List<UUID> generateStreamIds(final int numberOfStreams) {

        final List<UUID> streamIds = new ArrayList<>();
        for (int count = 0; count < numberOfStreams; count++) {
            streamIds.add(randomUUID());
        }

        return streamIds;
    }
}
