package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

public class RebuildPublishedEventFactory {

    @Inject
    private EventNumberGetter eventNumberGetter;

    @Inject
    private PublishedEventConverter publishedEventConverter;

    public PublishedEvent createPublishedEventFrom(final Event event, final AtomicLong previousEventNumber) {
        final Long eventNumber = eventNumberGetter.eventNumberFrom(event);

        final PublishedEvent publishedEvent = publishedEventConverter.toPublishedEvent(
                event,
                previousEventNumber.get());

        previousEventNumber.set(eventNumber);

        return publishedEvent;
    }
}
