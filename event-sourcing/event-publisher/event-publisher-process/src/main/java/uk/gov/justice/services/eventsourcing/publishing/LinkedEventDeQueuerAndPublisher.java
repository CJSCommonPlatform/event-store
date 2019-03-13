package uk.gov.justice.services.eventsourcing.publishing;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static uk.gov.justice.services.eventsourcing.EventDeQueuer.PUBLISH_TABLE_NAME;

import uk.gov.justice.services.eventsourcing.EventDeQueuer;
import uk.gov.justice.services.eventsourcing.EventFetcher;
import uk.gov.justice.services.eventsourcing.EventFetchingException;
import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * The EventDeQueuerAndPublisher class provides a method that returns an event from the EventDeQueuer
 * and publishes the event.
 */
public class LinkedEventDeQueuerAndPublisher {

    @Inject
    EventDeQueuer eventDeQueuer;

    @Inject
    EventPublisher eventPublisher;

    @Inject
    EventConverter eventConverter;

    @Inject
    EventFetcher eventFetcher;

    /**
     * Method that gets the next event to process from the EventDeQueuer,
     * converts the event to a JsonEnvelope type with the EventConverter
     * and then publishes the converted event with the EventPublisher.
     *
     * @return boolean
     */
    @Transactional(REQUIRES_NEW)
    public boolean deQueueAndPublish() {

        final Optional<UUID> eventId = eventDeQueuer.popNextEventId(PUBLISH_TABLE_NAME);
        if (eventId.isPresent()) {
            final Optional<LinkedEvent> linkedEvent = eventFetcher.getLinkedEvent(eventId.get());

            if(linkedEvent.isPresent()) {
                final JsonEnvelope jsonEnvelope = eventConverter.envelopeOf(linkedEvent.get());
                eventPublisher.publish(jsonEnvelope);

                return true;
            } else {
                throw new EventFetchingException(format("Failed to find LinkedEvent with id '%s'", eventId.get()));
            }

        }
        return false;
    }
}
