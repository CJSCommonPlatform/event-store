package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.slf4j.Logger;

public class PublishedEventInserter {

    private static final int NUMBER_OF_EVENTS_TO_LOG_AFTER = 1_000;
    
    @Inject
    private PublishedEventConverter publishedEventConverter;

    @Inject
    private PublishedEventRepository publishedEventRepository;

    @Inject
    private Logger logger;

    public int convertAndSave(final Event event, final AtomicLong previousEventNumber, final Set<UUID> activeStreamIds) {

        if (activeStreamIds.contains(event.getStreamId())) {
            final Long eventNumber = event.getEventNumber()
                    .orElseThrow(() -> new RebuildException(format("No eventNumber found for event with id '%s'", event.getId())));

            if (eventNumber > 0 && eventNumber % NUMBER_OF_EVENTS_TO_LOG_AFTER == 0) {
                logger.info(format("Inserted %d PublishedEvents...", eventNumber));
            }

            final PublishedEvent publishedEvent = publishedEventConverter.toPublishedEvent(
                    event,
                    previousEventNumber.get());

            publishedEventRepository.save(publishedEvent);

            previousEventNumber.set(eventNumber);

            return 1;
        }

        return 0;
    }
}
