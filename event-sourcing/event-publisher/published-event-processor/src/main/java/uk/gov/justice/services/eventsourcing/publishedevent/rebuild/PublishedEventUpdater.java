package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

public class PublishedEventUpdater {

    @Inject
    private EventJdbcRepository eventJdbcRepository;

    @Inject
    private ActiveEventStreamIdProvider activeEventStreamIdProvider;

    @Inject
    private  PublishedEventInserter publishedEventInserter;

    @Inject
    private Logger logger;

    @Transactional(REQUIRES_NEW)
    public void createPublishedEvents() {

        logger.info("Creating PublishedEvents..");

        final AtomicLong previousEventNumber = new AtomicLong(0);
        final Set<UUID> activeStreamIds = activeEventStreamIdProvider.getActiveStreamIds();

        try (final Stream<Event> eventStream = eventJdbcRepository.findAllOrderedByEventNumber()) {
            final int eventCount = eventStream
                    .mapToInt(event -> publishedEventInserter.convertAndSave(event, previousEventNumber, activeStreamIds))
                    .sum();

            logger.info(format("Inserted %d PublishedEvents", eventCount));
        }
    }
}
