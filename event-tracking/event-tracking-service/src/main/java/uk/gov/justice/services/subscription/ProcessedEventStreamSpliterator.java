package uk.gov.justice.services.subscription;

import static java.lang.Long.MAX_VALUE;

import java.util.Iterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ProcessedEventStreamSpliterator extends AbstractSpliterator<ProcessedEvent> {

    private final String source;
    private final String component;
    private final Long batchSize;
    private final ProcessedEventTrackingRepository processedEventTrackingRepository;

    private Iterator<ProcessedEvent> processedEventsIterator;
    private Long currentEventNumber = -1L;
    private Stream<ProcessedEvent> processedEventStream;

    public ProcessedEventStreamSpliterator(
            final String source,
            final String component,
            final Long batchSize,
            final ProcessedEventTrackingRepository processedEventTrackingRepository) {
        super(MAX_VALUE, ORDERED);
        this.source = source;
        this.component = component;

        this.batchSize = batchSize;
        this.processedEventTrackingRepository = processedEventTrackingRepository;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super ProcessedEvent> nextProcessedEventConsumer) {

        if (currentEventNumber < 0) {
            processedEventStream = processedEventTrackingRepository.getProcessedEventsLessThanEventNumberInDescendingOrder(
                    MAX_VALUE,
                    batchSize,
                    source,
                    component);

            processedEventsIterator = processedEventStream.iterator();
        }

        if (processedEventsIterator.hasNext()) {
            final ProcessedEvent nextProcessedEvent = processedEventsIterator.next();
            currentEventNumber = nextProcessedEvent.getPreviousEventNumber();

            nextProcessedEventConsumer.accept(nextProcessedEvent);

            return true;
        }

        if (currentEventNumber > 0) {
            closeSafely(processedEventStream);
            processedEventsIterator = processedEventTrackingRepository.getProcessedEventsLessThanEventNumberInDescendingOrder(
                            currentEventNumber + 1,
                            batchSize,
                            source,
                            component)
                    .iterator();

            final ProcessedEvent nextProcessedEvent = processedEventsIterator.next();
            currentEventNumber = nextProcessedEvent.getPreviousEventNumber();

            nextProcessedEventConsumer.accept(nextProcessedEvent);

            return true;
        }

        closeSafely(processedEventStream);

        return false;
    }

    private void closeSafely(final Stream<ProcessedEvent> theProcessedEventStream) {
        if (theProcessedEventStream != null) {
            theProcessedEventStream.close();
        }
    }
}
