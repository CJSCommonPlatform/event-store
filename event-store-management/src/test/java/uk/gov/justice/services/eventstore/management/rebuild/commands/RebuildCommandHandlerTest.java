package uk.gov.justice.services.eventstore.management.rebuild.commands;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.rebuild.events.RebuildCompleteEvent;
import uk.gov.justice.services.eventstore.management.rebuild.events.RebuildRequestedEvent;
import uk.gov.justice.services.jmx.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import java.time.ZonedDateTime;

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
    public void shouldFireShutterEventBeforeRebuild() throws Exception {

        final ZonedDateTime now = new UtcClock().now();
        final RebuildCommand rebuildCommand = new RebuildCommand();

        when(clock.now()).thenReturn(now);

        rebuildCommandHandler.doRebuild(rebuildCommand);

        verify(shutteringRequestedEventFirer).fire(new ShutteringRequestedEvent(rebuildCommand, now));
    }

    @Test
    public void shouldRebuildOnceShuttered() throws Exception {

        final RebuildCommand rebuildCommand = new RebuildCommand();
        final ShutteringCompleteEvent shutteringCompleteEvent = new ShutteringCompleteEvent(
                rebuildCommand,
                new UtcClock().now()
        );

        final ZonedDateTime unshutteringRequestedAt = new UtcClock().now();
        when(clock.now()).thenReturn(unshutteringRequestedAt);

        rebuildCommandHandler.onShutteringComplete(shutteringCompleteEvent);

        logger.info("Received ShutteringComplete event. Now firing RebuildRequestedEvent");

        rebuildRequestedEventEventFirer.fire(new RebuildRequestedEvent(unshutteringRequestedAt, rebuildCommand));
    }

    @Test
    public void shouldNotRebuildIfShutterCompleteEventTargetIsNotARebuildCommand() throws Exception {

        final SystemCommand someOtherCommand = mock(SystemCommand.class);

        rebuildCommandHandler.onShutteringComplete(new ShutteringCompleteEvent(
                someOtherCommand,
                new UtcClock().now()
        ));

        verifyZeroInteractions(logger);
        verifyZeroInteractions(rebuildRequestedEventEventFirer);
    }

    @Test
    public void shouldUnshutterOnceRebuilt() throws Exception {

        final RebuildCommand rebuildCommand = new RebuildCommand();
        final ZonedDateTime unshutteringRequestedAt = new UtcClock().now();

        when(clock.now()).thenReturn(unshutteringRequestedAt);

        rebuildCommandHandler.onRebuildComplete(new RebuildCompleteEvent(rebuildCommand, new UtcClock().now()));

        verify(logger).info("Received RebuildComplete event. Now firing UnshutteringRequested event");
        verify(unshutteringRequestedEventFirer).fire(new UnshutteringRequestedEvent(
                rebuildCommand,
                unshutteringRequestedAt));
    }
}
