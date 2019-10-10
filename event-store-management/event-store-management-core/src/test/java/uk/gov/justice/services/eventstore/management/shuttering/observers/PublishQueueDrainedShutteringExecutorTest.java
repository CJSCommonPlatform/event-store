package uk.gov.justice.services.eventstore.management.shuttering.observers;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventstore.management.shuttering.process.PublishQueueInterrogator;
import uk.gov.justice.services.jmx.api.command.ApplicationShutteringCommand;
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
public class PublishQueueDrainedShutteringExecutorTest {

    @Mock
    private PublishQueueInterrogator publishQueueInterrogator;

    @Mock
    private StopWatchFactory stopWatchFactory;

    @Mock
    private Logger logger;

    @InjectMocks
    private PublishQueueDrainedShutteringExecutor publishQueueDrainedShutteringExecutor;

    @Test
    public void shouldShutterButNotUnshutter() throws Exception {

        assertThat(publishQueueDrainedShutteringExecutor.shouldShutter(), is(true));
        assertThat(publishQueueDrainedShutteringExecutor.shouldUnshutter(), is(false));
    }

    @Test
    public void shouldWaitForPublishQueueToEmptyAndReturnSuccess() throws Exception {

        final UUID commandId = randomUUID();
        final ApplicationShutteringCommand applicationShutteringCommand = mock(ApplicationShutteringCommand.class);
        final StopWatch stopWatch = mock(StopWatch.class);

        when(stopWatchFactory.createStartedStopWatch()).thenReturn(stopWatch);
        when(publishQueueInterrogator.pollUntilPublishQueueEmpty()).thenReturn(true);

        final ShutteringResult shutteringResult = publishQueueDrainedShutteringExecutor.shutter(commandId, applicationShutteringCommand);

        assertThat(shutteringResult.getCommandId(), is(commandId));
        assertThat(shutteringResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(shutteringResult.getSystemCommand(), is(applicationShutteringCommand));
        assertThat(shutteringResult.getShutteringExecutorName(), is("PublishQueueDrainedShutteringExecutor"));
        assertThat(shutteringResult.getMessage(), is("Publish Queue drained successfully"));

        final InOrder inOrder = inOrder(logger, publishQueueInterrogator);

        inOrder.verify(logger).info("Waiting for Publish Queue to empty");
        inOrder.verify(publishQueueInterrogator).pollUntilPublishQueueEmpty();
        inOrder.verify(logger).info("Publish Queue drained successfully");

        verify(stopWatch).stop();
    }

    @Test
    public void shouldReturnFailureIfThePublishQueueFailsToDrainInTime() throws Exception {

        final UUID commandId = randomUUID();
        final ApplicationShutteringCommand applicationShutteringCommand = mock(ApplicationShutteringCommand.class);
        final StopWatch stopWatch = mock(StopWatch.class);

        when(stopWatchFactory.createStartedStopWatch()).thenReturn(stopWatch);
        when(publishQueueInterrogator.pollUntilPublishQueueEmpty()).thenReturn(false);
        when(stopWatch.getTime()).thenReturn(1234L);

        final ShutteringResult shutteringResult = publishQueueDrainedShutteringExecutor.shutter(commandId, applicationShutteringCommand);

        assertThat(shutteringResult.getCommandId(), is(commandId));
        assertThat(shutteringResult.getCommandState(), is(COMMAND_FAILED));
        assertThat(shutteringResult.getSystemCommand(), is(applicationShutteringCommand));
        assertThat(shutteringResult.getShutteringExecutorName(), is("PublishQueueDrainedShutteringExecutor"));
        assertThat(shutteringResult.getMessage(), is("PublishQueue failed to drain after 1234 milliseconds"));

        final InOrder inOrder = inOrder(stopWatch, logger);

        inOrder.verify(stopWatch).stop();
        inOrder.verify(stopWatch).getTime();
        inOrder.verify(logger).error("PublishQueue failed to drain after 1234 milliseconds");
    }
}
