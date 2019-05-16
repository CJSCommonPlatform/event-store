package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static uk.gov.justice.services.eventsourcing.publishedevent.jdbc.EventDeQueuer.PRE_PUBLISH_TABLE_NAME;

import uk.gov.justice.services.eventsourcing.publishedevent.PublishedEventException;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.EventDeQueuer;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class PrePublishProcessor {

    @Inject
    EventDeQueuer eventDeQueuer;

    @Inject
    EventPrePublisher eventPrePublisher;

    @Inject
    private EventJdbcRepository eventJdbcRepository;

    @Transactional(REQUIRES_NEW)
    public boolean prePublishNextEvent() {

        final Optional<UUID> eventId = eventDeQueuer.popNextEventId(PRE_PUBLISH_TABLE_NAME);

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
