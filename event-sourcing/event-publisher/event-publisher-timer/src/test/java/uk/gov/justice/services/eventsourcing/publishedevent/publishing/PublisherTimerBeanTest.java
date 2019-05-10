package uk.gov.justice.services.eventsourcing.publishedevent.publishing;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.publish.PublishedEventDeQueuerAndPublisher;
import uk.gov.justice.services.eventsourcing.util.jee.timer.TimerCanceler;
import uk.gov.justice.services.eventsourcing.util.jee.timer.TimerServiceManager;
import uk.gov.justice.services.jmx.lifecycle.ShutteringFlagProducerBean;

import javax.ejb.TimerService;

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
    private TimerCanceler timerCanceler;

    @Mock
    private ShutteringFlagProducerBean shutteringFlagProducerBean;

    @InjectMocks
    private PublisherTimerBean publisherTimerBean;

    @Test
    public void shouldSetUpTheTimerServiceOnPostConstruct() throws Exception {

        final long timerStartValue = 7000;
        final long timerIntervalValue = 2000;

        when(publisherTimerConfig.getTimerStartWaitMilliseconds()).thenReturn(timerStartValue);
        when(publisherTimerConfig.getTimerIntervalMilliseconds()).thenReturn(timerIntervalValue);

        publisherTimerBean.startTimerService();

        verify(timerCanceler).cancelTimer("event-store.de-queue-events-and-publish.job", timerService);
        verify(timerServiceManager).createIntervalTimer(
                "event-store.de-queue-events-and-publish.job",
                timerStartValue,
                timerIntervalValue,
                timerService);
    }

    @Test
    public void shouldRunPublishUntilAllEventsArePublished() throws Exception {

        when(publishedEventDeQueuerAndPublisher.deQueueAndPublish()).thenReturn(true, true, false);
        when(shutteringFlagProducerBean.isDoShuttering()).thenReturn(false);

        publisherTimerBean.doDeQueueAndPublish();

        verify(publishedEventDeQueuerAndPublisher, times(3)).deQueueAndPublish();
        verify(timerServiceManager, times(2)).cancelOverlappingTimers("event-store.de-queue-events-and-publish.job", 10, timerService);
    }

    @Test
    public void shouldNotPublishIfShutteringIsOn() throws Exception {

        when(shutteringFlagProducerBean.isDoShuttering()).thenReturn(true);

        publisherTimerBean.doDeQueueAndPublish();

        verify(timerServiceManager).cancelOverlappingTimers("event-store.de-queue-events-and-publish.job", 10, timerService);
        verify(publishedEventDeQueuerAndPublisher, never()).deQueueAndPublish();
    }
}
