package uk.gov.justice.services.subscription;

import static java.lang.Long.MAX_VALUE;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;
import uk.gov.justice.services.eventsourcing.util.messaging.EventSourceNameCalculator;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;
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
    private Logger logger;

    public void trackProcessedEvent(final JsonEnvelope event, final String componentName) {

        final Metadata metadata = event.metadata();
        final UUID id = metadata.id();
        final Long previousEventNumber = metadata
                .previousEventNumber()
                .orElseThrow(() -> new ProcessedEventTrackingException(format("Missing previous event number for event with id '%s'", id)));
        final Long eventNumber = metadata
                .eventNumber()
                .orElseThrow(() -> new ProcessedEventTrackingException(format("Missing event number for event with id '%s'", id)));

        final String source = eventSourceNameCalculator.getSource(event);

        final ProcessedEventTrackItem processedEventTrackItem = new ProcessedEventTrackItem(
                previousEventNumber,
                eventNumber,
                source,
                componentName
        );

        processedEventTrackingRepository.save(processedEventTrackItem);
    }

    public Stream<MissingEventRange> getAllMissingEvents(final String eventSourceName, final String componentName) {

        final EventNumberAccumulator eventNumberAccumulator = new EventNumberAccumulator();

        final Optional<ProcessedEventTrackItem> latestProcessedEvent = processedEventTrackingRepository.getLatestProcessedEvent(eventSourceName, componentName);

        if (latestProcessedEvent.isPresent()) {
            notSeenEventsRange(latestProcessedEvent.get().getPreviousEventNumber(), eventNumberAccumulator);
        } else {
            notSeenEventsRange(1L, eventNumberAccumulator);
        }

        try (final Stream<ProcessedEventTrackItem> allProcessedEvents = processedEventTrackingRepository.getAllProcessedEventsDescendingOrder(eventSourceName, componentName)) {
            allProcessedEvents
                    .forEach(processedEventTrackItem -> findMissingRange(processedEventTrackItem, eventNumberAccumulator));
        }

        if (eventNumberAccumulator.isInitialised() && eventNumberAccumulator.getLastPreviousEventNumber() != FIRST_POSSIBLE_EVENT_NUMBER) {
            eventNumberAccumulator.addRangeFrom(FIRST_POSSIBLE_EVENT_NUMBER);
        }

        logger.info(createMessageMissingEventRanges(eventNumberAccumulator));

        return eventNumberAccumulator.getMissingEventRanges().stream();
    }

    public Long getLatestProcessedEventNumber(final String source, final String componentName) {

        return processedEventTrackingRepository.getLatestProcessedEvent(source, componentName)
                .map(ProcessedEventTrackItem::getEventNumber)
                .orElse(FIRST_POSSIBLE_EVENT_NUMBER);
    }

    private void notSeenEventsRange(final long currentPreviousEventNumber, final EventNumberAccumulator eventNumberAccumulator) {

        final long currentEventNumber = MAX_VALUE;

        if (eventNumberAccumulator.isInitialised() && eventNumberAccumulator.getLastPreviousEventNumber() != currentEventNumber) {
            eventNumberAccumulator.addRangeFrom(currentEventNumber);
        }

        eventNumberAccumulator.set(currentPreviousEventNumber, currentEventNumber);
    }

    private void findMissingRange(final ProcessedEventTrackItem processedEventTrackItem, final EventNumberAccumulator eventNumberAccumulator) {

        final long currentEventNumber = processedEventTrackItem.getEventNumber();
        final long currentPreviousEventNumber = processedEventTrackItem.getPreviousEventNumber();

        if (eventNumberAccumulator.isInitialised() && eventNumberAccumulator.getLastPreviousEventNumber() != currentEventNumber) {
            eventNumberAccumulator.addRangeFrom(currentEventNumber);
        }

        eventNumberAccumulator.set(currentPreviousEventNumber, currentEventNumber);
    }

    private String createMessageMissingEventRanges(final EventNumberAccumulator eventNumberAccumulator) {

        return "Missing Event Ranges: [" +
                lineSeparator() +
                eventNumberAccumulator
                        .getMissingEventRanges()
                        .stream()
                        .map(MissingEventRange::toString)
                        .collect(joining("," + lineSeparator())) +
                lineSeparator() +
                "]";
    }
}
