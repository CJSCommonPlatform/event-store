package uk.gov.justice.services.eventsourcing.prepublish;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static uk.gov.justice.services.eventsourcing.EventDeQueuer.PRE_PUBLISH_TABLE_NAME;

import uk.gov.justice.services.eventsourcing.EventDeQueuer;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;

import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class PrePublishProcessor {

    @Inject
    EventDeQueuer eventDeQueuer;

    @Inject
    EventPrePublisher prePublishDelegate;

    @Transactional(REQUIRES_NEW)
    public boolean prePublishNextEvent() {

        final Optional<Event> eventOptional = eventDeQueuer.popNextEvent(PRE_PUBLISH_TABLE_NAME);

        if (eventOptional.isPresent()) {
            prePublishDelegate.prePublish(eventOptional.get());
            return true;
        }

        return false;
    }
}
