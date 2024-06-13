package uk.gov.justice.services.eventstore.management.replay.process;

import static javax.ejb.TransactionManagementType.CONTAINER;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Stateless
@TransactionManagement(CONTAINER)
public class ReplayEventToEventListenerProcessorBean {

    @Inject
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Inject
    private EventBufferProcessorFactory eventBufferProcessorFactory;

    @Inject
    private EventConverter eventConverter;


    @Transactional(REQUIRES_NEW)
    public void perform(final ReplayEventContext replayEventContext) {
        final String eventSourceName = replayEventContext.getEventSourceName();
        final UUID eventId = replayEventContext.getCommandRuntimeId();
        final String componentName = replayEventContext.getComponentName();

        final PublishedEvent publishedEvent = fetchPublishedEvent(eventSourceName, eventId);
        processWithEventBuffer(componentName, publishedEvent);
    }

    private PublishedEvent fetchPublishedEvent(String eventSourceName, UUID eventId) {
        final PublishedEventSource publishedEventSource = publishedEventSourceProvider.getPublishedEventSource(eventSourceName);

        return publishedEventSource.findByEventId(eventId)
                .orElseThrow(() -> new ReplayEventFailedException("Published event not found for given commandRuntimeId:" + eventId + " under event source name:" + eventSourceName));
    }

    private void processWithEventBuffer(String componentName, PublishedEvent publishedEvent) {
        final JsonEnvelope eventEnvelope = eventConverter.envelopeOf(publishedEvent);
        final EventBufferProcessor eventBufferProcessor = eventBufferProcessorFactory.create(componentName);
        eventBufferProcessor.processWithEventBuffer(eventEnvelope);
    }
}
