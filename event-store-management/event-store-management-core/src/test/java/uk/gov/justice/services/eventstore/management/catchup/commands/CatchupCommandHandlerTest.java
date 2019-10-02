package uk.gov.justice.services.eventstore.management.catchup.commands;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.EVENT_CATCHUP;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.INDEX_CATCHUP;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupRequestedEvent;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class CatchupCommandHandlerTest {

    @Mock
    private Event<CatchupRequestedEvent> catchupRequestedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private CatchupCommandHandler catchupCommandHandler;

    @Test
    public void shouldFireEventCatchup() throws Exception {

        final UUID commandId = randomUUID();
        final CatchupCommand catchupCommand = new CatchupCommand();
        final ZonedDateTime now = of(2019, 8, 23, 11, 22, 1, 0, UTC);

        when(clock.now()).thenReturn(now);

        catchupCommandHandler.catchupEvents(catchupCommand, commandId);

        verify(logger).info("Received command 'CATCHUP' at 11:22:01 AM");
        verify(catchupRequestedEventFirer).fire(new CatchupRequestedEvent(commandId, EVENT_CATCHUP, catchupCommand, now));
    }

    @Test
    public void shouldFireIndexCatchup() throws Exception {

        final UUID commandId = randomUUID();
        final IndexerCatchupCommand indexerCatchupCommand = new IndexerCatchupCommand();
        final ZonedDateTime now = of(2019, 8, 23, 11, 22, 1, 0, UTC);

        when(clock.now()).thenReturn(now);

        catchupCommandHandler.catchupSearchIndexes(indexerCatchupCommand, commandId);

        verify(logger).info("Received command 'INDEXER_CATCHUP' at 11:22:01 AM");
        verify(catchupRequestedEventFirer).fire(new CatchupRequestedEvent(commandId, INDEX_CATCHUP, indexerCatchupCommand, now));
    }
}
