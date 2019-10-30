package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;

import javax.inject.Inject;

public class ProcessCompleteDecider {

    @Inject
    private EventJdbcRepository eventJdbcRepository;

    public boolean isProcessingComplete(final BatchProcessDetails batchProcessDetails) {

        final long currentEventNumber = batchProcessDetails.getCurrentEventNumber().get();

        final long maximumEventNumber = eventJdbcRepository.getMaximumEventNumber();

        return currentEventNumber >= maximumEventNumber;
    }
}
