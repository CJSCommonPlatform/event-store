package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.util.stream.Collectors.toSet;
import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class ActiveEventStreamIdProvider {

    @Inject
    private EventStreamJdbcRepository eventStreamJdbcRepository;

    @Transactional(REQUIRED)
    public Set<UUID> getActiveStreamIds() {

        try(final Stream<EventStream> activeEventStreamStream = eventStreamJdbcRepository.findActive()) {

            return activeEventStreamStream
                    .map(EventStream::getStreamId)
                    .collect(toSet());
        }
    }
}
