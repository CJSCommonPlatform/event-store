package uk.gov.justice.services.eventstore.management.indexer.commands;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;
import uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class IndexerCatchupCommandHandlerTest {

    @Mock
    private Event<IndexerCatchupRequestedEvent> catchupRequestedEventEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private IndexerCatchupCommandHandler indexerCatchupCommandHandler;

    @Test
    public void shouldFireIndexerCatchupEvent() throws Exception {

        final IndexerCatchupCommand catchupCommand = new IndexerCatchupCommand();
        final ZonedDateTime now = of(2019, 8, 23, 11, 22, 1, 0, UTC);

        when(clock.now()).thenReturn(now);

        indexerCatchupCommandHandler.catchupSearchIndexes(catchupCommand);

        verify(logger).info("Received command 'INDEXER_CATCHUP' at 11:22:01 AM");
        verify(catchupRequestedEventEventFirer).fire(new IndexerCatchupRequestedEvent(catchupCommand, now));
    }
}
