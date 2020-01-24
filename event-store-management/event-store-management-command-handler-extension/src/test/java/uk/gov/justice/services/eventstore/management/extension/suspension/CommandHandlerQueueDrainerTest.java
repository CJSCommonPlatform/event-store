package uk.gov.justice.services.eventstore.management.extension.suspension;

import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;

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
public class CommandHandlerQueueDrainerTest {

    @Mock
    private CommandHandlerQueueInterrogator commandHandlerQueueInterrogator;

    @Mock
    private StopWatchFactory stopWatchFactory;

    @Mock
    private Logger logger;

    @InjectMocks
    private CommandHandlerQueueDrainer commandHandlerQueueDrainer;

    @Test
    public void shouldSuspendButNotUnsuspend() throws Exception {

        assertThat(commandHandlerQueueDrainer.shouldSuspend(), is(true));
        assertThat(commandHandlerQueueDrainer.shouldUnsuspend(), is(false));
    }

    @Test
    public void shouldWaitForCommandHandlerQueueToDrainAndReturnSuccess() throws Exception {

        final UUID commandId = randomUUID();
        final StopWatch stopWatch = mock(StopWatch.class);
        final SuspensionCommand suspensionCommand = mock(SuspensionCommand.class);

        when(stopWatchFactory.createStartedStopWatch()).thenReturn(stopWatch);
        when(commandHandlerQueueInterrogator.pollUntilEmptyHandlerQueue()).thenReturn(true);


        final SuspensionResult suspensionResult = commandHandlerQueueDrainer.suspend(commandId, suspensionCommand);

        assertThat(suspensionResult.getCommandId(), is(commandId));
        assertThat(suspensionResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(suspensionResult.getSystemCommand(), is(suspensionCommand));
        assertThat(suspensionResult.getSuspendableName(), is("CommandHandlerQueueDrainer"));
        assertThat(suspensionResult.getMessage(), is("Command Handler Queue drained successfully"));

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

        final UUID commandId = randomUUID();
        final StopWatch stopWatch = mock(StopWatch.class);
        final SuspensionCommand applicationShutteringCommand = mock(SuspensionCommand.class);

        when(stopWatchFactory.createStartedStopWatch()).thenReturn(stopWatch);
        when(commandHandlerQueueInterrogator.pollUntilEmptyHandlerQueue()).thenReturn(false);
        when(stopWatch.getTime()).thenReturn(12345L);

        final SuspensionResult suspensionResult = commandHandlerQueueDrainer.suspend(commandId, applicationShutteringCommand);

        assertThat(suspensionResult.getCommandId(), is(commandId));
        assertThat(suspensionResult.getCommandState(), is(COMMAND_FAILED));
        assertThat(suspensionResult.getSystemCommand(), is(applicationShutteringCommand));
        assertThat(suspensionResult.getSuspendableName(), is("CommandHandlerQueueDrainer"));
        assertThat(suspensionResult.getMessage(), is("Failed to drain command handler queue in 12345 milliseconds"));
        assertThat(suspensionResult.getException(), is(empty()));

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
