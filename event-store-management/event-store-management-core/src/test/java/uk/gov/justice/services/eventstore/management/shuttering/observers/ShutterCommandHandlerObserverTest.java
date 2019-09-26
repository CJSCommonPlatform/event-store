package uk.gov.justice.services.eventstore.management.shuttering.observers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventstore.management.logging.MdcLogger;
import uk.gov.justice.services.eventstore.management.shuttering.process.CommandHandlerQueueInterrogator;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.observers.shuttering.ShutteringRegistry;

import java.util.function.Consumer;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ShutterCommandHandlerObserverTest {

    @Mock
    private ShutteringRegistry shutteringRegistry;

    @Mock
    private CommandHandlerQueueInterrogator commandHandlerQueueInterrogator;

    @Mock
    private StopWatchFactory stopWatchFactory;

    @Mock
    private MdcLogger mdcLogger;

    @Mock
    private Logger logger;

    @InjectMocks
    private ShutterCommandHandlerObserver shutterCommandHandlerObserver;

    private Consumer<Runnable> testConsumer = Runnable::run;

    @Test
    public void shouldPollUntilQueueEmptyThenInformTheShutteringRegistry() throws Exception {

        final StopWatch stopWatch = mock(StopWatch.class);
        final SystemCommand systemCommand = mock(SystemCommand.class);
        final ShutteringProcessStartedEvent shutteringProcessStartedEvent = mock(ShutteringProcessStartedEvent.class);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);
        when(stopWatchFactory.createStartedStopWatch()).thenReturn(stopWatch);
        when(commandHandlerQueueInterrogator.pollUntilEmptyHandlerQueue()).thenReturn(true);
        when(shutteringProcessStartedEvent.getTarget()).thenReturn(systemCommand);


        shutterCommandHandlerObserver.waitForCommandQueueToEmpty(shutteringProcessStartedEvent);

        final InOrder inOrder = inOrder(
                logger,
                stopWatch,
                commandHandlerQueueInterrogator,
                shutteringRegistry);

        inOrder.verify(logger).info("Shuttering Command Handler. Waiting for queue to drain");
        inOrder.verify(commandHandlerQueueInterrogator).pollUntilEmptyHandlerQueue();
        inOrder.verify(logger).info("Command Handler Queue empty");
        inOrder.verify(shutteringRegistry).markShutteringCompleteFor(ShutterCommandHandlerObserver.class, systemCommand);
    }

    @Test
    public void shouldThrowExceptionIfQueueDoesNotDrain() throws Exception {

        final StopWatch stopWatch = mock(StopWatch.class);
        final SystemCommand systemCommand = mock(SystemCommand.class);
        final ShutteringProcessStartedEvent shutteringProcessStartedEvent = mock(ShutteringProcessStartedEvent.class);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);
        when(stopWatchFactory.createStartedStopWatch()).thenReturn(stopWatch);
        when(commandHandlerQueueInterrogator.pollUntilEmptyHandlerQueue()).thenReturn(false);
        when(shutteringProcessStartedEvent.getTarget()).thenReturn(systemCommand);
        when(stopWatch.getTime()).thenReturn(12345L);

        try {
            shutterCommandHandlerObserver.waitForCommandQueueToEmpty(shutteringProcessStartedEvent);
            fail();
        } catch (final ShutteringException expected) {
            assertThat(expected.getMessage(), is("Failed to drain command handler queue in 12345 milliseconds"));
        }

        verify(stopWatch).stop();

        verifyZeroInteractions(shutteringRegistry);
    }
}
