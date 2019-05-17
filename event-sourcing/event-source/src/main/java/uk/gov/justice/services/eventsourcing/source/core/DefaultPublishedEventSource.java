package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MultipleDataSourcePublishedEventRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

public class DefaultPublishedEventSource implements PublishedEventSource {

    private final EventConverter eventConverter;
    private final MultipleDataSourcePublishedEventRepository multipleDataSourcePublishedEventRepository;

    public DefaultPublishedEventSource(
            final MultipleDataSourcePublishedEventRepository multipleDataSourcePublishedEventRepository,
            final EventConverter eventConverter) {
        this.multipleDataSourcePublishedEventRepository = multipleDataSourcePublishedEventRepository;
        this.eventConverter = eventConverter;
    }

    @Override
    public Stream<JsonEnvelope> findEventsSince(final long eventNumber) {
        return multipleDataSourcePublishedEventRepository.findEventsSince(eventNumber)
                .map(eventConverter::envelopeOf);
    }
}
