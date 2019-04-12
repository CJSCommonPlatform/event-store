package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

public class JdbcBasedPublishedEventSource implements PublishedEventSource {

    private final EventRepository eventRepository;
    private final EventConverter eventConverter;

    public JdbcBasedPublishedEventSource(
            final EventRepository eventRepository,
            final EventConverter eventConverter) {
        this.eventRepository = eventRepository;
        this.eventConverter = eventConverter;
    }

    @Override
    public Stream<JsonEnvelope> findEventsSince(final long eventNumber) {

        return eventRepository.findEventsSince(eventNumber)
                .map(eventConverter::envelopeOf);
    }
}
