package uk.gov.justice.services.eventstore.management.indexer.commands;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupCompletedEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;
import uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class IndexerCatchupCommandHandlerTest {
    @Mock
    private Event<ShutteringRequestedEvent> shutteringRequestedEventFirer;

    @Mock
    private Event<IndexerCatchupRequestedEvent> indexerCatchupRequestedEventEvent;

    @Mock
    private Event<UnshutteringRequestedEvent> unshutteringRequestedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private IndexerCatchupCommandHandler indexerCatchupCommandHandler;

    @Test
    public void shouldCallIndexerCatchupOnHandlingCommand() throws Exception {

        final ZonedDateTime now = new UtcClock().now();
        final IndexerCatchupCommand indexerCatchupCommand= new IndexerCatchupCommand();

        when(clock.now()).thenReturn(now);

        indexerCatchupCommandHandler.doCatchupWhilstShuttered(indexerCatchupCommand);

        final InOrder inOrder = inOrder(logger, shutteringRequestedEventFirer);

        inOrder.verify(logger).info("Indexer Catchup requested. Shuttering application first");
        inOrder.verify(shutteringRequestedEventFirer).fire(new ShutteringRequestedEvent(
                indexerCatchupCommand,
                now));
    }

    @Test
    public void shouldKickOffIndexerCatchupWhenShutteringCompleteIfCommandIsShutterCatchupCommand() throws Exception {

        final ShutteringCompleteEvent shutteringCompleteEvent = mock(ShutteringCompleteEvent.class);
        final IndexerCatchupCommand catchupCommand = new IndexerCatchupCommand();

        when(shutteringCompleteEvent.getTarget()).thenReturn(catchupCommand);

        indexerCatchupCommandHandler.onShutteringComplete(shutteringCompleteEvent);

        final InOrder inOrder = inOrder(logger, indexerCatchupRequestedEventEvent);

        inOrder.verify(logger).info("Received ShutteringComplete event. Now firing IndexerCatchupRequested event");
        inOrder.verify(indexerCatchupRequestedEventEvent).fire(new IndexerCatchupRequestedEvent(
                catchupCommand,
                clock.now()));
    }

    @Test
    public void shouldNotKickOffIndexerCatchupIfCommandIsNotShutterCatchupCommand() throws Exception {

        final ShutteringCompleteEvent shutteringCompleteEvent = mock(ShutteringCompleteEvent.class);
        final SystemCommand notAShutterCatchupCommand = mock(SystemCommand.class);

        when(shutteringCompleteEvent.getTarget()).thenReturn(notAShutterCatchupCommand);

        indexerCatchupCommandHandler.onShutteringComplete(shutteringCompleteEvent);

        verifyZeroInteractions(logger);
        verifyZeroInteractions(indexerCatchupRequestedEventEvent);
    }

    @Test
    public void shouldKickOffUnshutteringWhenCatchupCompleteIfCommandIsShutterCatchupCommand() throws Exception {

        final IndexerCatchupCompletedEvent catchupCompletedEvent = mock(IndexerCatchupCompletedEvent.class);
        final IndexerCatchupCommand catchupCommand = new IndexerCatchupCommand();

        when(catchupCompletedEvent.getTarget()).thenReturn(catchupCommand);

        indexerCatchupCommandHandler.onCatchupComplete(catchupCompletedEvent);

        final InOrder inOrder = inOrder(logger, unshutteringRequestedEventFirer);

        inOrder.verify(logger).info("Received IndexerCatchupCompleted event. Now firing UnshutteringRequested event");
        inOrder.verify(unshutteringRequestedEventFirer).fire(new UnshutteringRequestedEvent(
                catchupCommand,
                clock.now()));
    }
}
