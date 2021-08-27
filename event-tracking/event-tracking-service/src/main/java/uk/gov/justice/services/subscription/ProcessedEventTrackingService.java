package uk.gov.justice.services.subscription;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;
import uk.gov.justice.services.eventsourcing.util.messaging.EventSourceNameCalculator;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;

public class ProcessedEventTrackingService {

    private static final long FIRST_POSSIBLE_EVENT_NUMBER = 0L;

    @Inject
    private ProcessedEventTrackingRepository processedEventTrackingRepository;

    @Inject
    private EventSourceNameCalculator eventSourceNameCalculator;

    @Inject
    private MissingEventRangeFinder missingEventRangeFinder;

    @Inject
    private EventRangeNormalizer eventRangeNormalizer;

    @Inject
    private Logger logger;

    public void trackProcessedEvent(final JsonEnvelope event, final String componentName) {

        final Metadata metadata = event.metadata();
        final UUID eventId = metadata.id();
        final Long previousEventNumber = metadata
                .previousEventNumber()
                .orElseThrow(() -> new ProcessedEventTrackingException(format("Missing previous event number for event with id '%s'", eventId)));
        final Long eventNumber = metadata
                .eventNumber()
                .orElseThrow(() -> new ProcessedEventTrackingException(format("Missing event number for event with id '%s'", eventId)));

        final String source = eventSourceNameCalculator.getSource(event);

        final ProcessedEvent processedEvent = new ProcessedEvent(
                eventId, previousEventNumber,
                eventNumber,
                source,
                componentName
        );

        processedEventTrackingRepository.save(processedEvent);
    }

    public Stream<MissingEventRange> getAllMissingEvents(final String eventSourceName, final String componentName, final Long highestPublishedEventNumber) {

        final LinkedList<MissingEventRange> missingEventRanges = missingEventRangeFinder.getRangesOfMissingEvents(eventSourceName, componentName, highestPublishedEventNumber);
        final List<MissingEventRange> normalizedEventRanges = eventRangeNormalizer.normalize(missingEventRanges);

        logger.info(createMessageMissingEventRanges(normalizedEventRanges));

        return normalizedEventRanges.stream();
    }

    public Long getLatestProcessedEventNumber(final String source, final String componentName) {

        return processedEventTrackingRepository.getLatestProcessedEvent(source, componentName)
                .map(ProcessedEvent::getEventNumber)
                .orElse(FIRST_POSSIBLE_EVENT_NUMBER);
    }
    
    private String createMessageMissingEventRanges(final List<MissingEventRange> missingEventRanges) {

        return "Missing Event Ranges: [" +
                lineSeparator() +
                missingEventRanges
                        .stream()
                        .map(MissingEventRange::toString)
                        .collect(joining("," + lineSeparator())) +
                lineSeparator() +
                "]";
    }
}
