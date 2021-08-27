package uk.gov.justice.services.eventstore.management.catchup.process;

import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;

import java.util.stream.Stream;

import javax.inject.Inject;

public class MissingEventStreamer {

    @Inject
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Inject
    private ProcessedEventTrackingService processedEventTrackingService;

    @Inject
    private HighestPublishedEventNumberProvider highestPublishedEventNumberProvider;

    public Stream<PublishedEvent> getMissingEvents(final String eventSourceName, final String componentName) {

        final PublishedEventSource publishedEventSource = publishedEventSourceProvider.getPublishedEventSource(eventSourceName);
        final Long highestPublishedEventNumber = highestPublishedEventNumberProvider.getHighestPublishedEventNumber();

        return processedEventTrackingService.getAllMissingEvents(eventSourceName, componentName, highestPublishedEventNumber)
                .flatMap(publishedEventSource::findEventRange);
    }
}
