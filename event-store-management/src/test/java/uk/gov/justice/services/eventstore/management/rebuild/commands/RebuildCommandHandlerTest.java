package uk.gov.justice.services.eventstore.management.rebuild.commands;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.rebuild.events.RebuildRequestedEvent;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RebuildCommandHandlerTest {

    @Mock
    private UtcClock clock;

    @Mock
    private Event<RebuildRequestedEvent> rebuildRequestedEventEventFirer;

    @InjectMocks
    private RebuildCommandHandler rebuildCommandHandler;

    @Test
    public void shouldFireRebuildEvent() throws Exception {

        final ZonedDateTime now = new UtcClock().now();
        final RebuildCommand rebuildCommand = new RebuildCommand();

        when(clock.now()).thenReturn(now);

        rebuildCommandHandler.doRebuild(rebuildCommand);

        verify(rebuildRequestedEventEventFirer).fire(new RebuildRequestedEvent(now, rebuildCommand));
    }
}
