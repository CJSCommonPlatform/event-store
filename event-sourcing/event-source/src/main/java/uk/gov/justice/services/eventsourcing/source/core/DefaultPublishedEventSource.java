package uk.gov.justice.services.eventsourcing.source.core;

import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MultipleDataSourcePublishedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;
import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.transaction.Transactional;

public class DefaultPublishedEventSource implements PublishedEventSource {

    private final MultipleDataSourcePublishedEventRepository multipleDataSourcePublishedEventRepository;

    public DefaultPublishedEventSource(final MultipleDataSourcePublishedEventRepository multipleDataSourcePublishedEventRepository) {
        this.multipleDataSourcePublishedEventRepository = multipleDataSourcePublishedEventRepository;
    }

    @Override
    public Stream<PublishedEvent> findEventsSince(final long eventNumber) {
        return multipleDataSourcePublishedEventRepository.findEventsSince(eventNumber);
    }

    @Transactional(REQUIRED)
    @Override
    public Stream<PublishedEvent> findEventRange(final MissingEventRange missingEventRange) {

        final Long fromEventNumber = missingEventRange.getMissingEventFrom();
        final Long toEventNumber = missingEventRange.getMissingEventTo();

        return multipleDataSourcePublishedEventRepository.findEventRange(fromEventNumber, toEventNumber);
    }

    @Transactional(REQUIRED)
    @Override
    public Optional<PublishedEvent> findByEventId(final UUID eventId) {
        return multipleDataSourcePublishedEventRepository.findByEventId(eventId);
    }

    @Transactional(REQUIRED)
    @Override
    public Long getHighestPublishedEventNumber() {
        final Optional<PublishedEvent> latestPublishedEvent = multipleDataSourcePublishedEventRepository
                .getLatestPublishedEvent();

        return latestPublishedEvent.map(publishedEvent -> publishedEvent
                        .getEventNumber()
                        .orElse(0L))
                .orElse(0L);
    }
}
