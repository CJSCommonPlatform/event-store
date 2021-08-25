package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.eventsourcing.repository.jdbc.PrePublishQueueRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.PublishedEventException;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class PrePublishProcessor {

    @Inject
    private PrePublishQueueRepository prePublishQueueRepository;

    @Inject
    private EventPrePublisher eventPrePublisher;

    @Inject
    private EventJdbcRepository eventJdbcRepository;

    @Transactional(REQUIRES_NEW)
    public boolean prePublishNextEvent() {

        final Optional<UUID> eventId = prePublishQueueRepository.popNextEventId();

        if (eventId.isPresent()) {
            final Optional<Event> event = eventJdbcRepository.findById(eventId.get());

            if(event.isPresent()) {
                eventPrePublisher.prePublish(event.get());
                return true;

            } else {
                throw new PublishedEventException(format("Failed to find Event with id '%s'", eventId.get()));
            }
        }

        return false;
    }
}
