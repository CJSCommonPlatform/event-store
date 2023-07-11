package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static org.mockito.Mockito.inOrder;

import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventTableCleaner;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber.EventNumberRenumberer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublishedEventRebuilderTest {

    @Mock
    private EventNumberRenumberer eventNumberRenumberer;

    @Mock
    private PublishedEventTableCleaner publishedEventTableCleaner;

    @Mock
    private PublishedEventUpdater publishedEventUpdater;

    @InjectMocks
    private PublishedEventRebuilder publishedEventRebuilder;

    @Test
    public void shouldRenumberEventsAndSaveNewPublishedEvents() throws Exception {

        publishedEventRebuilder.rebuild();

        final InOrder inOrder = inOrder(
                eventNumberRenumberer,
                publishedEventTableCleaner,
                publishedEventUpdater
        );

        inOrder.verify(eventNumberRenumberer).renumberEventLogEventNumber();
        inOrder.verify(publishedEventTableCleaner).deleteAll();
        inOrder.verify(publishedEventUpdater).createPublishedEvents();
    }
}
