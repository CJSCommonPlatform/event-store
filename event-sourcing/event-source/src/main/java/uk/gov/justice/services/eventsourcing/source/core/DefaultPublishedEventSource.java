package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MultipleDataSourcePublishedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;
import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;

import java.util.stream.Stream;

public class DefaultPublishedEventSource implements PublishedEventSource {

    private final MultipleDataSourcePublishedEventRepository multipleDataSourcePublishedEventRepository;

    public DefaultPublishedEventSource(final MultipleDataSourcePublishedEventRepository multipleDataSourcePublishedEventRepository) {
        this.multipleDataSourcePublishedEventRepository = multipleDataSourcePublishedEventRepository;
    }

    @Override
    public Stream<PublishedEvent> findEventsSince(final long eventNumber) {
        return multipleDataSourcePublishedEventRepository.findEventsSince(eventNumber);
    }

    @Override
    public Stream<PublishedEvent> findEventRange(final MissingEventRange missingEventRange) {

        final Long fromEventNumber = missingEventRange.getMissingEventFrom();
        final Long toEventNumber = missingEventRange.getMissingEventTo();

        return multipleDataSourcePublishedEventRepository.findEventRange(fromEventNumber, toEventNumber);
    }
}
