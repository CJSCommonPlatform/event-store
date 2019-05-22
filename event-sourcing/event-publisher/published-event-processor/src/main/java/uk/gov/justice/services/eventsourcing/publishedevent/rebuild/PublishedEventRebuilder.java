package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventRepository;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventTableCleaner;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class PublishedEventRebuilder {

    @Inject
    private EventNumberRenumberer eventNumberRenumberer;

    @Inject
    private EventJdbcRepository eventJdbcRepository;

    @Inject
    private PublishedEventTableCleaner publishedEventTableCleaner;

    @Inject
    private PublishedEventConverter publishedEventConverter;

    @Inject
    private PublishedEventRepository publishedEventRepository;

    @Inject
    private ActiveEventStreamIdProvider activeEventStreamIdProvider;

    @Transactional(REQUIRED)
    public void rebuild() {

        eventNumberRenumberer.renumberEventLogEventNumber();
        publishedEventTableCleaner.deleteAll();

        final AtomicLong previousEventNumber = new AtomicLong(0);
        final Set<UUID> activeStreamIds = activeEventStreamIdProvider.getActiveStreamIds();

        try(final Stream<Event> eventStream = eventJdbcRepository.findAllOrderedByEventNumber()) {
            eventStream.forEach(event -> convertAndSave(event, previousEventNumber, activeStreamIds));
        }
    }

    private void convertAndSave(final Event event, final AtomicLong previousEventNumber, final Set<UUID> activeStreamIds) {

        if (activeStreamIds.contains(event.getStreamId())) {
            final Long eventNumber = event.getEventNumber()
                    .orElseThrow(() -> new RebuildException(format("No eventNumber found for event with id '%s'", event.getId())));

            final PublishedEvent publishedEvent = publishedEventConverter.toPublishedEvent(
                    event,
                    previousEventNumber.get());

            publishedEventRepository.save(publishedEvent);

            previousEventNumber.set(eventNumber);
        }
    }
}
