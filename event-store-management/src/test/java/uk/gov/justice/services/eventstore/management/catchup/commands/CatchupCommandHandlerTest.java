package uk.gov.justice.services.eventstore.management.catchup.commands;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupCommandHandlerTest {

    @Mock
    private Event<CatchupRequestedEvent> catchupRequestedEventFirer;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private CatchupCommandHandler catchup;

    @Test
    public void shouldFireCatchupEvent() {

        final ZonedDateTime requestedAt = new UtcClock().now();

        when(clock.now()).thenReturn(requestedAt);

        catchup.doCatchup();

        verify(catchupRequestedEventFirer).fire(new CatchupRequestedEvent(new CatchupCommand(), requestedAt));
    }
}
