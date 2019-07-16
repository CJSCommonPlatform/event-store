package uk.gov.justice.services.eventstore.management.shuttering.observers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventstore.management.shuttering.process.PublishQueueInterrogator;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.observers.shuttering.ShutteringRegistry;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class PublishQueueDrainedShutteringObserverTest {

    @Mock
    private PublishQueueInterrogator publishQueueInterrogator;

    @Mock
    private StopWatchFactory stopWatchFactory;

    @Mock
    private ShutteringRegistry shutteringRegistry;

    @Mock
    private Logger logger;

    @InjectMocks
    private PublishQueueDrainedShutteringObserver publishQueueDrainedShutteringObserver;

    @Test
    public void shouldWaitForPublishQueueToEmpty() throws Exception {

        final SystemCommand systemCommand = mock(SystemCommand.class);
        final ShutteringProcessStartedEvent shutteringProcessStartedEvent = mock(ShutteringProcessStartedEvent.class);

        when(shutteringProcessStartedEvent.getTarget()).thenReturn(systemCommand);
        when(publishQueueInterrogator.pollUntilPublishQueueEmpty()).thenReturn(true);

        publishQueueDrainedShutteringObserver.waitForPublishQueueToEmpty(shutteringProcessStartedEvent);

        final InOrder inOrder = inOrder(logger, publishQueueInterrogator, shutteringRegistry);

        inOrder.verify(logger).info("Waiting for Publish Queue to empty");
        inOrder.verify(publishQueueInterrogator).pollUntilPublishQueueEmpty();
        inOrder.verify(logger).info("Publish Queue empty");
        inOrder.verify(shutteringRegistry).markShutteringCompleteFor(PublishQueueDrainedShutteringObserver.class, systemCommand);
    }

    @Test
    public void shouldThrowExceptionIfThePublishQueueFailsToDrain() throws Exception {

        final SystemCommand systemCommand = mock(SystemCommand.class);
        final ShutteringProcessStartedEvent shutteringProcessStartedEvent = mock(ShutteringProcessStartedEvent.class);
        final StopWatch stopWatch = mock(StopWatch.class);

        when(shutteringProcessStartedEvent.getTarget()).thenReturn(systemCommand);
        when(stopWatchFactory.createStartedStopWatch()).thenReturn(stopWatch);
        when(publishQueueInterrogator.pollUntilPublishQueueEmpty()).thenReturn(false);
        when(stopWatch.getTime()).thenReturn(1234L);

        try {
            publishQueueDrainedShutteringObserver.waitForPublishQueueToEmpty(shutteringProcessStartedEvent);
            fail();
        } catch (final ShutteringException expected) {
            assertThat(expected.getMessage(), is("PublishQueue failed to drain after 1234 milliseconds"));
        }

        verify(stopWatch).stop();
    }
}
