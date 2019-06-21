package uk.gov.justice.services.eventstore.management.indexer.commands;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IndexerCatchupCommandHandlerTest {

    @Mock
    private Event<IndexerCatchupRequestedEvent> indexerCatchupRequestedEventFirer;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private IndexerCatchupCommandHandler indexerCatchup;

    @Test
    public void shouldFireCatchupEvent() {

        final ZonedDateTime requestedAt = new UtcClock().now();
        final IndexerCatchupCommand indexerCatchupCommand = new IndexerCatchupCommand();

        when(clock.now()).thenReturn(requestedAt);

        indexerCatchup.doCatchup(indexerCatchupCommand);

        verify(indexerCatchupRequestedEventFirer).fire(new IndexerCatchupRequestedEvent(indexerCatchupCommand, requestedAt));
    }
}
