package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AsynchronousPrePublisherTest {

    @Mock
    private PrePublisherTimerConfig prePublisherTimerConfig;

    @Mock
    private PrePublishProcessor prePublishProcessor;

    @Mock
    private StopWatchFactory stopWatchFactory;

    @InjectMocks
    private AsynchronousPrePublisher asynchronousPrePublisher;

    @Test
    public void shouldRunPublishUntilAllEventsArePublished() throws Exception {

        final long timerMaxRuntimeValue = 495L;

        when(prePublisherTimerConfig.getTimerMaxRuntimeMilliseconds()).thenReturn(timerMaxRuntimeValue);
        when(stopWatchFactory.createStopWatch()).thenReturn(mock(StopWatch.class));
        when(prePublishProcessor.prePublishNextEvent()).thenReturn(true, true, false);

        asynchronousPrePublisher.performPrePublish();

        verify(prePublishProcessor, times(3)).prePublishNextEvent();
    }

    @Test
    public void shouldRunPublishUntilTimeRunsOut() throws Exception {

        final long timerIntervalValue = 2000L;
        final long timerMaxRuntimeValue = 495L;
        final StopWatch stopWatch = mock(StopWatch.class);

        when(prePublisherTimerConfig.getTimerMaxRuntimeMilliseconds()).thenReturn(timerMaxRuntimeValue);
        when(stopWatchFactory.createStopWatch()).thenReturn(stopWatch);
        when(prePublishProcessor.prePublishNextEvent()).thenReturn(true, true, true);
        when(stopWatch.getTime()).thenReturn(timerIntervalValue);

        asynchronousPrePublisher.performPrePublish();

        verify(prePublishProcessor, times(1)).prePublishNextEvent();
    }
}
