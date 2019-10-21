package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.util.jee.timer.TimerServiceManager;

import javax.ejb.TimerService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrePublisherTimerBeanTest {

    @Mock
    private TimerService timerService;

    @Mock
    private PrePublisherTimerConfig prePublisherTimerConfig;

    @Mock
    private TimerServiceManager timerServiceManager;

    @Mock
    private AsynchronousPrePublisher asynchronousPrePublisher;

    @InjectMocks
    private PrePublisherTimerBean prePublisherTimerBean;

    @Test
    public void shouldSetUpTheTimerServiceOnPostConstruct() throws Exception {

        final long timerStartValue = 7250L;
        final long timerIntervalValue = 2000L;

        when(prePublisherTimerConfig.getTimerStartWaitMilliseconds()).thenReturn(timerStartValue);
        when(prePublisherTimerConfig.getTimerIntervalMilliseconds()).thenReturn(timerIntervalValue);

        prePublisherTimerBean.startTimerService();

        verify(timerServiceManager).createIntervalTimer(
                "event-store.pre-publish-events.job",
                timerStartValue,
                timerIntervalValue,
                timerService);
    }

    @Test
    public void shouldRunPrePublishAsynchronously() throws Exception {

        when(prePublisherTimerConfig.isDisabled()).thenReturn(false);

        prePublisherTimerBean.performPrePublish();

        verify(asynchronousPrePublisher).performPrePublish();
    }

    @Test
    public void shouldNeverRunPrePublishIfDisabled() throws Exception {

        when(prePublisherTimerConfig.isDisabled()).thenReturn(true);

        prePublisherTimerBean.performPrePublish();

        verifyZeroInteractions(asynchronousPrePublisher);
    }
}
