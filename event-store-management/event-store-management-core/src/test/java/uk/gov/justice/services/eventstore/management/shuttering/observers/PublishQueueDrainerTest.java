package uk.gov.justice.services.eventstore.management.shuttering.observers;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventstore.management.shuttering.process.PublishQueueInterrogator;
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;

import java.util.UUID;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class PublishQueueDrainerTest {

    @Mock
    private PublishQueueInterrogator publishQueueInterrogator;

    @Mock
    private StopWatchFactory stopWatchFactory;

    @Mock
    private Logger logger;

    @InjectMocks
    private PublishQueueDrainer publishQueueDrainer;

    @Test
    public void shouldSuspendButNotUnsuspend() throws Exception {

        assertThat(publishQueueDrainer.shouldSuspend(), is(true));
        assertThat(publishQueueDrainer.shouldUnsuspend(), is(false));
    }

    @Test
    public void shouldWaitForPublishQueueToEmptyAndReturnSuccess() throws Exception {

        final UUID commandId = randomUUID();
        final SuspensionCommand suspensionCommand = mock(SuspensionCommand.class);
        final StopWatch stopWatch = mock(StopWatch.class);

        when(stopWatchFactory.createStartedStopWatch()).thenReturn(stopWatch);
        when(publishQueueInterrogator.pollUntilPublishQueueEmpty()).thenReturn(true);

        final SuspensionResult suspensionResult = publishQueueDrainer.suspend(commandId, suspensionCommand);

        assertThat(suspensionResult.getCommandId(), is(commandId));
        assertThat(suspensionResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(suspensionResult.getSystemCommand(), is(suspensionCommand));
        assertThat(suspensionResult.getSuspendableName(), is("PublishQueueDrainer"));
        assertThat(suspensionResult.getMessage(), is("Publish Queue drained successfully"));

        final InOrder inOrder = inOrder(logger, publishQueueInterrogator);

        inOrder.verify(logger).info("Waiting for Publish Queue to empty");
        inOrder.verify(publishQueueInterrogator).pollUntilPublishQueueEmpty();
        inOrder.verify(logger).info("Publish Queue drained successfully");

        verify(stopWatch).stop();
    }

    @Test
    public void shouldReturnFailureIfThePublishQueueFailsToDrainInTime() throws Exception {

        final UUID commandId = randomUUID();
        final SuspensionCommand applicationShutteringCommand = mock(SuspensionCommand.class);
        final StopWatch stopWatch = mock(StopWatch.class);

        when(stopWatchFactory.createStartedStopWatch()).thenReturn(stopWatch);
        when(publishQueueInterrogator.pollUntilPublishQueueEmpty()).thenReturn(false);
        when(stopWatch.getTime()).thenReturn(1234L);

        final SuspensionResult suspensionResult = publishQueueDrainer.suspend(commandId, applicationShutteringCommand);

        assertThat(suspensionResult.getCommandId(), is(commandId));
        assertThat(suspensionResult.getCommandState(), is(COMMAND_FAILED));
        assertThat(suspensionResult.getSystemCommand(), is(applicationShutteringCommand));
        assertThat(suspensionResult.getSuspendableName(), is("PublishQueueDrainer"));
        assertThat(suspensionResult.getMessage(), is("PublishQueue failed to drain after 1234 milliseconds"));

        final InOrder inOrder = inOrder(stopWatch, logger);

        inOrder.verify(stopWatch).stop();
        inOrder.verify(stopWatch).getTime();
        inOrder.verify(logger).error("PublishQueue failed to drain after 1234 milliseconds");
    }
}
