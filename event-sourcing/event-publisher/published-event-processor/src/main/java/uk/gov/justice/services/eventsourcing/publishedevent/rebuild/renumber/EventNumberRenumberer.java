package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.NEVER;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

public class EventNumberRenumberer {

    @Inject
    private BatchEventRenumberer batchEventRenumberer;

    @Inject
    private EventNumberSequenceResetter eventNumberSequenceResetter;

    @Inject
    private Logger logger;

    @Transactional(NEVER)
    public void renumberEventLogEventNumber() {
        
        eventNumberSequenceResetter.resetSequence();

        logger.info("Renumbering events in the event_log table...");

        final List<EventIdBatch> eventIdBatches = batchEventRenumberer
                .getEventIdsOrderedByCreationDate();

        final int total = eventIdBatches.stream()
                .mapToInt(batchEventRenumberer::renumberEvents)
                .sum();

        logger.info(format("Renumbered %d events in total", total));
    }
}
