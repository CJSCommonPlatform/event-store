package uk.gov.justice.services.subscription;

import javax.inject.Inject;

public class ProcessedEventStreamSpliteratorFactory {

    @Inject
    private ProcessedEventTrackingRepository processedEventTrackingRepository;

    public ProcessedEventStreamSpliterator getProcessedEventStreamSpliterator(
            final String source,
            final String component,
            final Long processedEventFetchBatchSize) {
        
        return new ProcessedEventStreamSpliterator(
                source,
                component,
                processedEventFetchBatchSize,
                processedEventTrackingRepository
        );
    }}
