package uk.gov.justice.services.eventsourcing.prepublish;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static uk.gov.justice.services.eventsourcing.EventDeQueuer.PRE_PUBLISH_TABLE_NAME;

import uk.gov.justice.services.eventsourcing.EventDeQueuer;
import uk.gov.justice.services.eventsourcing.EventFetcher;
import uk.gov.justice.services.eventsourcing.EventFetchingException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;

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
    EventFetcher eventFetcher;

    @Transactional(REQUIRES_NEW)
    public boolean prePublishNextEvent() {

        final Optional<UUID> eventId = eventDeQueuer.popNextEventId(PRE_PUBLISH_TABLE_NAME);

        if (eventId.isPresent()) {
            final Optional<Event> event = eventFetcher.getEvent(eventId.get());

            if(event.isPresent()) {
                eventPrePublisher.prePublish(event.get());
                return true;

            } else {
                throw new EventFetchingException(format("Failed to find Event with id '%s'", eventId.get()));
            }
        }

        return false;
    }
}
