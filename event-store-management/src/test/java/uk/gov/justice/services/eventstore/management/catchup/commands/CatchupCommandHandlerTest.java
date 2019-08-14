package uk.gov.justice.services.eventstore.management.catchup.commands;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;

import java.time.ZonedDateTime;

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
    public void shouldFireCatchupEvent() throws Exception {

        final CatchupCommand catchupCommand = new CatchupCommand();
        final ZonedDateTime now = of(2019, 8, 23, 11, 22, 1, 0, UTC);

        when(clock.now()).thenReturn(now);

        catchupCommandHandler.catchupEvents(catchupCommand);

        verify(logger).info("Received command 'CATCHUP' at 11:22:01 AM");
        verify(catchupRequestedEventFirer).fire(new CatchupRequestedEvent(catchupCommand, now));
    }
}
