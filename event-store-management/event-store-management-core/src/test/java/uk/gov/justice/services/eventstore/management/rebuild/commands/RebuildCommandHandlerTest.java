package uk.gov.justice.services.eventstore.management.rebuild.commands;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.events.rebuild.RebuildRequestedEvent;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

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
public class RebuildCommandHandlerTest {

    @Mock
    private Event<ShutteringRequestedEvent> shutteringRequestedEventFirer;

    @Mock
    private Event<RebuildRequestedEvent> rebuildRequestedEventEventFirer;

    @Mock
    private Event<UnshutteringRequestedEvent> unshutteringRequestedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private RebuildCommandHandler rebuildCommandHandler;

    @Test
    public void shouldFireRebuildEvent() throws Exception {

        final UUID commandId = randomUUID();
        final RebuildCommand rebuildCommand = new RebuildCommand();
        final ZonedDateTime now = of(2019, 8, 23, 11, 22, 1, 0, UTC);

        when(clock.now()).thenReturn(now);

        rebuildCommandHandler.doRebuild(rebuildCommand, commandId);

        verify(logger).info("Received command 'REBUILD' at 11:22:01 AM");
        verify(rebuildRequestedEventEventFirer).fire(new RebuildRequestedEvent(now, rebuildCommand));
    }
}
