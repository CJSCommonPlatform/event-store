package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinder;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

public class DefaultPublishedEventSource implements PublishedEventSource {

    private final EventConverter eventConverter;
    private final PublishedEventFinder publishedEventFinder;

    public DefaultPublishedEventSource(
            final PublishedEventFinder publishedEventFinder,
            final EventConverter eventConverter) {
        this.publishedEventFinder = publishedEventFinder;
        this.eventConverter = eventConverter;
    }

    @Override
    public Stream<JsonEnvelope> findEventsSince(final long eventNumber) {
        return publishedEventFinder.findEventsSince(eventNumber)
                .map(eventConverter::envelopeOf);
    }
}
