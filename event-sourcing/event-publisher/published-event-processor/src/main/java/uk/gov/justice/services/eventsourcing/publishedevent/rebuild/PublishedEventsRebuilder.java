package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javax.inject.Inject;

public class PublishedEventsRebuilder {

    @Inject
    private EventNumberGetter eventNumberGetter;

    @Inject
    private BatchedPublishedEventInserterFactory batchedPublishedEventInserterFactory;

    @Inject
    private ActiveEventFilter activeEventFilter;

    @Inject
    private RebuildPublishedEventFactory rebuildPublishedEventFactory;

    @SuppressWarnings("squid:S3864")
    public List<PublishedEvent> rebuild(
            final Stream<Event> eventStream,
            final AtomicLong previousEventNumber,
            final AtomicLong currentEventNumber,
            final Set<UUID> activeStreamIds) {

        try (final BatchedPublishedEventInserter batchedPublishedEventInserter = batchedPublishedEventInserterFactory.createInitialised()) {
            final List<PublishedEvent> publishedEvents = eventStream
                    .peek(event -> currentEventNumber.set(eventNumberGetter.eventNumberFrom(event)))
                    .filter(event -> activeEventFilter.isActiveEvent(event, activeStreamIds))
                    .map(event -> rebuildPublishedEventFactory.createPublishedEventFrom(event, previousEventNumber))
                    .map(batchedPublishedEventInserter::addToBatch)
                    .collect(toList());

            batchedPublishedEventInserter.insertBatch();

            return publishedEvents;
        }
    }
}
