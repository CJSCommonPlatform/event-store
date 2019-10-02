package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupProcessingOfEventFailedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;

import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class EventProcessingFailedHandler {

    @Inject
    private Event<CatchupProcessingOfEventFailedEvent> catchupProcessingOfEventFailedEventFirer;

    @Inject
    private Logger logger;

    public void handle(
            final RuntimeException exception, final PublishedEvent publishedEvent, final String subscriptionName, final CatchupType catchupType, final UUID commandId) {

        final String logMessage = format("Failed to process publishedEvent with metadata: %s", publishedEvent.getMetadata());
        logger.error(
                logMessage,
                exception);

        final CatchupProcessingOfEventFailedEvent catchupProcessingOfEventFailedEvent = new CatchupProcessingOfEventFailedEvent(
                commandId,
                publishedEvent.getId(),
                publishedEvent.getMetadata(),
                exception,
                catchupType,
                subscriptionName
        );

        catchupProcessingOfEventFailedEventFirer.fire(catchupProcessingOfEventFailedEvent);
    }
}
