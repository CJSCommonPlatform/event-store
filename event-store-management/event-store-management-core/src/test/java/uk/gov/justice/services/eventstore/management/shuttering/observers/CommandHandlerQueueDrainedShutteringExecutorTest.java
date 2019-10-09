package uk.gov.justice.services.eventstore.management.shuttering.observers;

import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventstore.management.shuttering.process.CommandHandlerQueueInterrogator;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;

import java.util.UUID;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class CommandHandlerQueueDrainedShutteringExecutorTest {

    @Mock
    private CommandHandlerQueueInterrogator commandHandlerQueueInterrogator;

    @Mock
    private StopWatchFactory stopWatchFactory;

    @Mock
    private Logger logger;

    @InjectMocks
    private CommandHandlerQueueDrainedShutteringExecutor commandHandlerQueueDrainedShutteringExecutor;

    @Test
    public void shouldShutterButNotUnshutter() throws Exception {

        assertThat(commandHandlerQueueDrainedShutteringExecutor.shouldShutter(), is(true));
        assertThat(commandHandlerQueueDrainedShutteringExecutor.shouldUnshutter(), is(false));
    }

    @Test
    public void shouldWaitForCommandHandlerQueueToDrainAndReturnSuccess() throws Exception {

        final UUID commandId = UUID.randomUUID();
        final StopWatch stopWatch = mock(StopWatch.class);
        final SystemCommand systemCommand = mock(SystemCommand.class);

        when(stopWatchFactory.createStartedStopWatch()).thenReturn(stopWatch);
        when(commandHandlerQueueInterrogator.pollUntilEmptyHandlerQueue()).thenReturn(true);


        final ShutteringResult shutteringResult = commandHandlerQueueDrainedShutteringExecutor.shutter(commandId, systemCommand);

        assertThat(shutteringResult.getCommandId(), is(commandId));
        assertThat(shutteringResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(shutteringResult.getSystemCommand(), is(systemCommand));
        assertThat(shutteringResult.getShutteringExecutorName(), is("CommandHandlerQueueDrainedShutteringExecutor"));
        assertThat(shutteringResult.getMessage(), is("Command Handler Queue drained successfully"));

        final InOrder inOrder = inOrder(
                logger,
                stopWatch,
                commandHandlerQueueInterrogator);

        inOrder.verify(logger).info("Shuttering Command Handler. Waiting for queue to drain");
        inOrder.verify(commandHandlerQueueInterrogator).pollUntilEmptyHandlerQueue();
        inOrder.verify(logger).info("Command Handler Queue drained successfully");
    }

    @Test
    public void shouldReturnFailureIfQueueDoesNotDrainInTime() throws Exception {

        final UUID commandId = UUID.randomUUID();
        final StopWatch stopWatch = mock(StopWatch.class);
        final SystemCommand systemCommand = mock(SystemCommand.class);

        when(stopWatchFactory.createStartedStopWatch()).thenReturn(stopWatch);
        when(commandHandlerQueueInterrogator.pollUntilEmptyHandlerQueue()).thenReturn(false);
        when(stopWatch.getTime()).thenReturn(12345L);

        final ShutteringResult shutteringResult = commandHandlerQueueDrainedShutteringExecutor.shutter(commandId, systemCommand);

        assertThat(shutteringResult.getCommandId(), is(commandId));
        assertThat(shutteringResult.getCommandState(), is(COMMAND_FAILED));
        assertThat(shutteringResult.getSystemCommand(), is(systemCommand));
        assertThat(shutteringResult.getShutteringExecutorName(), is("CommandHandlerQueueDrainedShutteringExecutor"));
        assertThat(shutteringResult.getMessage(), is("Failed to drain command handler queue in 12345 milliseconds"));
        assertThat(shutteringResult.getException(), is(empty()));

        final InOrder inOrder = inOrder(
                logger,
                stopWatch,
                commandHandlerQueueInterrogator);

        inOrder.verify(logger).info("Shuttering Command Handler. Waiting for queue to drain");
        inOrder.verify(commandHandlerQueueInterrogator).pollUntilEmptyHandlerQueue();
        inOrder.verify(stopWatch).stop();
        inOrder.verify(logger).error("Failed to drain command handler queue in 12345 milliseconds");
    }
}
