package uk.gov.justice.services.eventsourcing.publishedevent;

import uk.gov.justice.services.eventsourcing.publishedevent.publish.PublishedEventsProcessor;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;

import java.util.stream.Stream;

import javax.inject.Inject;

public class ActiveStreamsRepublisher {

    @Inject
    private EventStreamJdbcRepository eventStreamJdbcRepository;

    @Inject
    private PublishedEventsProcessor publishedEventsProcessor;

    @Inject
    private EventJdbcRepository eventJdbcRepository;

    public void populatePublishedEvents() {

        try (final Stream<EventStream> activeStreams = eventStreamJdbcRepository.findActive()) {
            activeStreams.forEach(stream -> publishedEventsProcessor.populatePublishedEvents(
                    stream.getStreamId(),
                    eventJdbcRepository));
        }
    }
}
