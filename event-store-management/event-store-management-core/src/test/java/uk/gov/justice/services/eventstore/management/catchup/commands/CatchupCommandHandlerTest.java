package uk.gov.justice.services.eventstore.management.catchup.commands;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.commands.EventCatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.IndexerCatchupCommand;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupRequestedEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.enterprise.event.Event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
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
        final EventCatchupCommand eventCatchupCommand = new EventCatchupCommand();
        final ZonedDateTime now = of(2019, 8, 23, 11, 22, 1, 0, UTC);

        when(clock.now()).thenReturn(now);

        catchupCommandHandler.catchupEvents(eventCatchupCommand, commandId);

        verify(logger).info("Received command 'CATCHUP' at 11:22:01 AM");
        verify(catchupRequestedEventFirer).fire(new CatchupRequestedEvent(commandId, eventCatchupCommand, now));
    }

    @Test
    public void shouldFireIndexCatchup() throws Exception {

        final UUID commandId = randomUUID();
        final IndexerCatchupCommand indexerCatchupCommand = new IndexerCatchupCommand();
        final ZonedDateTime now = of(2019, 8, 23, 11, 22, 1, 0, UTC);

        when(clock.now()).thenReturn(now);

        catchupCommandHandler.catchupSearchIndexes(indexerCatchupCommand, commandId);

        verify(logger).info("Received command 'INDEXER_CATCHUP' at 11:22:01 AM");
        verify(catchupRequestedEventFirer).fire(new CatchupRequestedEvent(commandId, indexerCatchupCommand, now));
    }
}
