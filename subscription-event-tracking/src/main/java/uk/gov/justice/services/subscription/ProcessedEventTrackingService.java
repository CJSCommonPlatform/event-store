package uk.gov.justice.services.subscription;

import static java.lang.String.format;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

public class ProcessedEventTrackingService {

    private static final long FIRST_POSSIBLE_EVENT_NUMBER = 0L;

    @Inject
    ProcessedEventTrackingRepository processedEventTrackingRepository;

    public void trackProcessedEvent(final JsonEnvelope event) {

        final Metadata metadata = event.metadata();
        final UUID id = metadata.id();
        final Long previousEventNumber = metadata
                .previousEventNumber()
                .orElseThrow(() -> new ProcessedEventTrackingException(format("Missing previous event number for event with id '%s'", id)));
        final Long eventNumber = metadata
                .eventNumber()
                .orElseThrow(() -> new ProcessedEventTrackingException(format("Missing event number for event with id '%s'", id)));
        final String source = metadata
                .source()
                .orElseThrow(() -> new ProcessedEventTrackingException(format("No source found in event with id '%s'", id)));

        final ProcessedEventTrackItem processedEventTrackItem = new ProcessedEventTrackItem(
                previousEventNumber,
                eventNumber,
                source
        );

        processedEventTrackingRepository.save(processedEventTrackItem);
    }

    public List<MissingEventRange> getAllMissingEvents(final String source) {

        final EventNumberAccumulator eventNumberAccumulator = new EventNumberAccumulator();

        try(final Stream<ProcessedEventTrackItem> allProcessedEvents = processedEventTrackingRepository.getAllProcessedEvents(source)) {
            allProcessedEvents
                    .forEach(processedEventTrackItem -> findMissingRange(processedEventTrackItem, eventNumberAccumulator));
        }

        if (eventNumberAccumulator.isInitialised() && eventNumberAccumulator.getLastPreviousEventNumber() != FIRST_POSSIBLE_EVENT_NUMBER) {
            eventNumberAccumulator.addRangeFrom(FIRST_POSSIBLE_EVENT_NUMBER);
        }

        return eventNumberAccumulator.getMissingEventRanges();
    }

    public Long getLatestProcessedEventNumber(final String source) {

        return processedEventTrackingRepository.getLatestProcessedEvent(source)
                .map(ProcessedEventTrackItem::getEventNumber)
                .orElse(FIRST_POSSIBLE_EVENT_NUMBER);
    }

    private void findMissingRange(final ProcessedEventTrackItem processedEventTrackItem, final EventNumberAccumulator eventNumberAccumulator) {

        final long currentEventNumber = processedEventTrackItem.getEventNumber();
        final long currentPreviousEventNumber = processedEventTrackItem.getPreviousEventNumber();

        if (eventNumberAccumulator.isInitialised() && eventNumberAccumulator.getLastPreviousEventNumber() != currentEventNumber) {
            eventNumberAccumulator.addRangeFrom(currentEventNumber);
        }

        eventNumberAccumulator.set(currentPreviousEventNumber, currentEventNumber);
    }
}
