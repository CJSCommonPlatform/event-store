package uk.gov.justice.services.eventsourcing.publishedevent.publishing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.publish.PublishedEventDeQueuerAndPublisher;
import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventsourcing.util.jee.timer.TimerServiceManager;

import javax.ejb.TimerService;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublisherTimerBeanTest {

    @Mock
    private TimerService timerService;

    @Mock
    private PublisherTimerConfig publisherTimerConfig;

    @Mock
    private PublishedEventDeQueuerAndPublisher publishedEventDeQueuerAndPublisher;

    @Mock
    private TimerServiceManager timerServiceManager;

    @Mock
    private StopWatchFactory stopWatchFactory;

    @InjectMocks
    private PublisherTimerBean publisherTimerBean;

    @Test
    public void shouldSetUpTheTimerServiceOnPostConstruct() throws Exception {

        final long timerStartValue = 7000L;
        final long timerIntervalValue = 2000L;

        when(publisherTimerConfig.getTimerStartWaitMilliseconds()).thenReturn(timerStartValue);
        when(publisherTimerConfig.getTimerIntervalMilliseconds()).thenReturn(timerIntervalValue);

        publisherTimerBean.startTimerService();

        verify(timerServiceManager).createIntervalTimer(
                "event-store.de-queue-events-and-publish.job",
                timerStartValue,
                timerIntervalValue,
                timerService);
    }

    @Test
    public void shouldRunPublishUntilAllEventsArePublished() throws Exception {

        final long timerIntervalValue = 2000L;
        final long timerMaxRuntimeValue = 495L;

        when(publisherTimerConfig.getTimerIntervalMilliseconds()).thenReturn(timerIntervalValue);
        when(publisherTimerConfig.getTimerMaxRuntimeMilliseconds()).thenReturn(timerMaxRuntimeValue);
        when(stopWatchFactory.createStopWatch()).thenReturn(mock(StopWatch.class));
        when(publishedEventDeQueuerAndPublisher.deQueueAndPublish()).thenReturn(true, true, false);

        publisherTimerBean.doDeQueueAndPublish();

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

        publisherTimerBean.doDeQueueAndPublish();

        verify(publishedEventDeQueuerAndPublisher, times(1)).deQueueAndPublish();
    }
}
