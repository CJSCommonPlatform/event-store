package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

public class PublishedEventInserter {

    @Inject
    private PublishedEventConverter publishedEventConverter;

    @Inject
    private PublishedEventRepository publishedEventRepository;

    public Optional<PublishedEvent> convertAndSave(final Event event, final AtomicLong previousEventNumber, final Set<UUID> activeStreamIds) {

        if (activeStreamIds.contains(event.getStreamId())) {
            final Long eventNumber = event.getEventNumber()
                    .orElseThrow(() -> new RebuildException(format("No eventNumber found for event with id '%s'", event.getId())));

            final PublishedEvent publishedEvent = publishedEventConverter.toPublishedEvent(
                    event,
                    previousEventNumber.get());

            publishedEventRepository.save(publishedEvent);

            previousEventNumber.set(eventNumber);

            return of(publishedEvent);
        }

        return empty();
    }
}
