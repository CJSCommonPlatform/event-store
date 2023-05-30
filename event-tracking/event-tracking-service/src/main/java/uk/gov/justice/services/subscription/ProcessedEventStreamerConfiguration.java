package uk.gov.justice.services.subscription;

import static java.lang.Long.parseLong;

import uk.gov.justice.services.common.configuration.GlobalValue;

import javax.inject.Inject;

public class ProcessedEventStreamerConfiguration {

    @Inject
    @GlobalValue(key = "catchup.fetch.processed.event.batch.size", defaultValue = "100000")
    private String processedEventFetchBatchSize;

    public Long getProcessedEventFetchBatchSize() {
        return parseLong(processedEventFetchBatchSize);
    }
}
