package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventNumberRenumbererTest {

    @Mock
    private BatchEventRenumberer batchEventRenumberer;

    @Mock
    private EventNumberSequenceResetter eventNumberSequenceResetter;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventNumberRenumberer eventNumberRenumberer;

    @Test
    public void shouldUpdateSequenceAndRenumberEventsInBatches() throws Exception {

        final EventIdBatch eventIdBatch_1 = mock(EventIdBatch.class);
        final EventIdBatch eventIdBatch_2 = mock(EventIdBatch.class);
        final EventIdBatch eventIdBatch_3 = mock(EventIdBatch.class);

        final List<EventIdBatch> eventIdBatches = asList(
                eventIdBatch_1,
                eventIdBatch_2,
                eventIdBatch_3
        );

        when(batchEventRenumberer.getEventIdsOrderedByCreationDate()).thenReturn(eventIdBatches);
        when(batchEventRenumberer.renumberEvents(eventIdBatch_1)).thenReturn(1);
        when(batchEventRenumberer.renumberEvents(eventIdBatch_2)).thenReturn(2);
        when(batchEventRenumberer.renumberEvents(eventIdBatch_3)).thenReturn(3);

        eventNumberRenumberer.renumberEventLogEventNumber();

        final InOrder inOrder = inOrder(
                eventNumberSequenceResetter,
                logger,
                batchEventRenumberer
        );

        inOrder.verify(eventNumberSequenceResetter).resetSequence();
        inOrder.verify(logger).info("Renumbering events in the event_log table...");
        inOrder.verify(batchEventRenumberer).getEventIdsOrderedByCreationDate();
        inOrder.verify(batchEventRenumberer).renumberEvents(eventIdBatch_1);
        inOrder.verify(batchEventRenumberer).renumberEvents(eventIdBatch_2);
        inOrder.verify(batchEventRenumberer).renumberEvents(eventIdBatch_3);
        inOrder.verify(logger).info("Renumbered 6 events in total");
    }
}
