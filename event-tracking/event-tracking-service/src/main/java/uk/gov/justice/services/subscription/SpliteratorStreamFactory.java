package uk.gov.justice.services.subscription;

import static java.util.stream.StreamSupport.stream;

import java.util.stream.Stream;

public class SpliteratorStreamFactory {

    public Stream<ProcessedEvent> createStreamFrom(final ProcessedEventStreamSpliterator processedEventStreamSpliterator) {
        return stream(processedEventStreamSpliterator, false);
    }
}
