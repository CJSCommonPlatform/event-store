package uk.gov.justice.services.eventsourcing.prepublish;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.timer.TimerCanceler;
import uk.gov.justice.services.eventsourcing.timer.TimerServiceManager;

import javax.ejb.TimerService;

import org.junit.Test;
import org.junit.runner.RunWith;
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
    private TimerCanceler timerCanceler;

    @Mock
    private PrePublishProcessor prePublishProcessor;

    @InjectMocks
    private PrePublishTimerBean prePublishTimerBean;

    @Test
    public void shouldSetUpTheTimerServiceOnPostConstruct() throws Exception {

        final long timerStartValue = 7250;
        final long timerIntervalValue = 2000;

        when(prePublishTimerConfig.getTimerStartWaitMilliseconds()).thenReturn(timerStartValue);
        when(prePublishTimerConfig.getTimerIntervalMilliseconds()).thenReturn(timerIntervalValue);

        prePublishTimerBean.startTimerService();

        verify(timerCanceler).cancelTimer("framework.pre-publish-events.job", timerService);
        verify(timerServiceManager).createIntervalTimer(
                "framework.pre-publish-events.job",
                timerStartValue,
                timerIntervalValue,
                timerService);
    }

    @Test
    public void shouldRunPublishUntilAllEventsArePublished() throws Exception {

        when(prePublishProcessor.prePublishNextEvent()).thenReturn(true, true, false);

        prePublishTimerBean.performPrePublish();

        verify(prePublishProcessor, times(3)).prePublishNextEvent();
        verify(timerServiceManager, times(2)).cancelOverlappingTimers("framework.pre-publish-events.job", 10, timerService);
    }
}
