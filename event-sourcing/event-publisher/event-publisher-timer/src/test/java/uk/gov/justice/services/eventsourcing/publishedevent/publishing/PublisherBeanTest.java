package uk.gov.justice.services.eventsourcing.publishedevent.publishing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.publish.PublishedEventDeQueuerAndPublisher;
import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublisherBeanTest {

    @Mock
    private PublisherTimerConfig publisherTimerConfig;

    @Mock
    private PublishedEventDeQueuerAndPublisher publishedEventDeQueuerAndPublisher;

    @Mock
    private StopWatchFactory stopWatchFactory;

    @InjectMocks
    private PublisherBean publisherBean;

    @Test
    public void shouldRunPublishUntilAllEventsArePublished() throws Exception {

        final long timerMaxRuntimeValue = 495L;

        when(publisherTimerConfig.getTimerMaxRuntimeMilliseconds()).thenReturn(timerMaxRuntimeValue);
        when(stopWatchFactory.createStopWatch()).thenReturn(mock(StopWatch.class));
        when(publishedEventDeQueuerAndPublisher.deQueueAndPublish()).thenReturn(true, true, false);

        publisherBean.publishAsynchronously();

        verify(publishedEventDeQueuerAndPublisher, times(3)).deQueueAndPublish();
    }

    @Test
    public void shouldRunPublishUntilTimeRunsOut() throws Exception {

        final long timerIntervalValue = 2000L;
        final long timerMaxRuntimeValue = 459L;
        final StopWatch stopWatch = mock(StopWatch.class);

        when(publisherTimerConfig.getTimerIntervalMilliseconds()).thenReturn(timerIntervalValue);
        when(publisherTimerConfig.getTimerMaxRuntimeMilliseconds()).thenReturn(timerMaxRuntimeValue);
        when(stopWatchFactory.createStopWatch()).thenReturn(stopWatch);
        when(publishedEventDeQueuerAndPublisher.deQueueAndPublish()).thenReturn(true, true);
        when(stopWatch.getTime()).thenReturn(timerIntervalValue);

        publisherBean.publishAsynchronously();

        verify(publishedEventDeQueuerAndPublisher, times(1)).deQueueAndPublish();
    }
}
