package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.publishing.PublisherBean;
import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventsourcing.util.jee.timer.TimerServiceManager;

import javax.ejb.TimerService;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrePublishTimerBeanTest {

    @Mock
    private TimerService timerService;

    @Mock
    private PrePublishTimerConfig prePublishTimerConfig;

    @Mock
    private TimerServiceManager timerServiceManager;

    @Mock
    private PrePublishProcessor prePublishProcessor;

    @Mock
    private StopWatchFactory stopWatchFactory;

    @Mock
    private PublisherBean publisherBean;

    @InjectMocks
    private PrePublishTimerBean prePublishTimerBean;

    @Test
    public void shouldSetUpTheTimerServiceOnPostConstruct() throws Exception {

        final long timerStartValue = 7250L;
        final long timerIntervalValue = 2000L;

        when(prePublishTimerConfig.getTimerStartWaitMilliseconds()).thenReturn(timerStartValue);
        when(prePublishTimerConfig.getTimerIntervalMilliseconds()).thenReturn(timerIntervalValue);

        prePublishTimerBean.startTimerService();

        verify(timerServiceManager).createIntervalTimer(
                "event-store.pre-publish-events.job",
                timerStartValue,
                timerIntervalValue,
                timerService);
    }

    @Test
    public void shouldRunPublishUntilAllEventsArePublished() throws Exception {

        final long timerIntervalValue = 2000L;
        final long timerMaxRuntimeValue = 495L;

        when(prePublishTimerConfig.getTimerIntervalMilliseconds()).thenReturn(timerIntervalValue);
        when(prePublishTimerConfig.getTimerMaxRuntimeMilliseconds()).thenReturn(timerMaxRuntimeValue);
        when(stopWatchFactory.createStopWatch()).thenReturn(mock(StopWatch.class));
        when(prePublishProcessor.prePublishNextEvent()).thenReturn(true, true, false);

        prePublishTimerBean.performPrePublish();

        final InOrder inOrder = inOrder(prePublishProcessor, publisherBean);

        inOrder.verify(prePublishProcessor, times(3)).prePublishNextEvent();
        inOrder.verify(publisherBean).publishAsynchronously();
    }

    @Test
    public void shouldRunPublishUntilTimeRunsOut() throws Exception {

        final long timerIntervalValue = 2000L;
        final long timerMaxRuntimeValue = 495L;
        final StopWatch stopWatch = mock(StopWatch.class);

        when(prePublishTimerConfig.getTimerIntervalMilliseconds()).thenReturn(timerIntervalValue);
        when(prePublishTimerConfig.getTimerMaxRuntimeMilliseconds()).thenReturn(timerMaxRuntimeValue);
        when(stopWatchFactory.createStopWatch()).thenReturn(stopWatch);
        when(prePublishProcessor.prePublishNextEvent()).thenReturn(true, true, true);
        when(stopWatch.getTime()).thenReturn(timerIntervalValue);

        prePublishTimerBean.performPrePublish();

        final InOrder inOrder = inOrder(prePublishProcessor, publisherBean);

        inOrder.verify(prePublishProcessor, times(1)).prePublishNextEvent();
        inOrder.verify(publisherBean).publishAsynchronously();
    }
}
