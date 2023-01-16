package uk.gov.justice.services.eventsourcing.publishedevent.publishing;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.ejb.timer.TimerServiceManager;

import javax.ejb.TimerService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublisherTimerBeanTest {

    @Mock
    private TimerService timerService;

    @Mock
    private PublisherTimerConfig publisherTimerConfig;

    @Mock
    private TimerServiceManager timerServiceManager;

    @Mock
    private AsynchronousPublisher asynchronousPublisher;

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
    public void shouldRunPublishAsynchronously() throws Exception {

        when(publisherTimerConfig.isDisabled()).thenReturn(false);

        publisherTimerBean.doDeQueueAndPublish();

        verify(asynchronousPublisher).doDeQueueAndPublish();
    }

    @Test
    public void shouldRunPublishIfDisabled() throws Exception {

        when(publisherTimerConfig.isDisabled()).thenReturn(true);

        publisherTimerBean.doDeQueueAndPublish();

        verifyNoInteractions(asynchronousPublisher);
    }
}
