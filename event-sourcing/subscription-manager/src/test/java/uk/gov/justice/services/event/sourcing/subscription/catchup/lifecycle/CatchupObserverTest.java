package uk.gov.justice.services.event.sourcing.subscription.catchup.lifecycle;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupCompletedEvent;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupStartedEvent;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupStartedForSubscriptionEvent;

import java.time.Duration;
import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class CatchupObserverTest {

    @Mock
    private CatchupsInProgressCache catchupsInProgressCache;

    @Mock
    private CatchupDurationCalculator catchupDurationCalculator;

    @Mock
    private Event<CatchupCompletedEvent> catchupCompletedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private CatchupObserver catchupObserver;

    @Captor
    private ArgumentCaptor<CatchupInProgress> catchupInProgressCaptor;

    @Test
    public void shouldClearEventsInProgressOnCatchupStart() throws Exception {

        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        catchupObserver.onCatchupStarted(new CatchupStartedEvent(catchupStartedAt));

        verify(logger).info("Event catchup started at 2019-02-23T17:12:23Z");
        verify(logger).info("Performing catchup of events...");
        verify(catchupsInProgressCache).removeAll();
    }

    @Test
    public void shouldLogCatchupStartedForSubscriptionAndStoreProgress() throws Exception {

        final String subscriptionName = "EVENT_LISTENER";
        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent = new CatchupStartedForSubscriptionEvent(
                subscriptionName,
                catchupStartedAt);

        catchupObserver.onCatchupStartedForSubscription(catchupStartedForSubscriptionEvent);

         verify(catchupsInProgressCache).addCatchupInProgress(catchupInProgressCaptor.capture());
         verify(logger).info("Event catchup for subscription 'EVENT_LISTENER' started at 2019-02-23T17:12:23Z");

        final CatchupInProgress catchupInProgress = catchupInProgressCaptor.getValue();

        assertThat(catchupInProgress.getSubscriptionName(), is(subscriptionName));
        assertThat(catchupInProgress.getStartedAt(), is(catchupStartedAt));
    }

    @Test
    public void shouldRemoveTheCatchupForSubscriptionInProgressOnCatchupForSubscriptionComplete() throws Exception {


        final String subscriptionName = "EVENT_LISTENER";
        final ZonedDateTime catchupCompletedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final int totalNumberOfEvents = 23;

        final Duration catchupDuration = Duration.of(5_000, MILLIS);

        final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = new CatchupCompletedForSubscriptionEvent(
                subscriptionName,
                totalNumberOfEvents,
                catchupCompletedAt
        );

        final CatchupInProgress catchupInProgress = mock(CatchupInProgress.class);

        when(catchupsInProgressCache.removeCatchupInProgress(subscriptionName)).thenReturn(catchupInProgress);
        when(catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent)).thenReturn(catchupDuration);

        when(catchupsInProgressCache.noCatchupsInProgress()).thenReturn(false);

        catchupObserver.onCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

        verify(logger).info("Event catchup for subscription 'EVENT_LISTENER' completed at 2019-02-23T17:12:23Z");
        verify(logger).info("Event catchup for subscription 'EVENT_LISTENER' caught up 23 events");
        verify(logger).info("Event catchup for subscription 'EVENT_LISTENER' took 5000 milliseconds");

        verifyZeroInteractions(catchupCompletedEventFirer);
    }

    @Test
    public void shouldFireTheCatchupCompleteEventIfAllCatchupsForSubscriptionsComplete() throws Exception {


        final String subscriptionName = "EVENT_LISTENER";
        final ZonedDateTime catchupCompletedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final ZonedDateTime allCatchupsCompletedAt = catchupCompletedAt.plusSeconds(23);
        final int totalNumberOfEvents = 23;

        final Duration catchupDuration = Duration.of(5_000, MILLIS);

        final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = new CatchupCompletedForSubscriptionEvent(
                subscriptionName,
                totalNumberOfEvents,
                catchupCompletedAt
        );

        final CatchupInProgress catchupInProgress = mock(CatchupInProgress.class);

        when(catchupsInProgressCache.removeCatchupInProgress(subscriptionName)).thenReturn(catchupInProgress);
        when(catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent)).thenReturn(catchupDuration);

        when(catchupsInProgressCache.noCatchupsInProgress()).thenReturn(true);
        when(clock.now()).thenReturn(allCatchupsCompletedAt);

        catchupObserver.onCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

        verify(logger).info("Event catchup for subscription 'EVENT_LISTENER' completed at 2019-02-23T17:12:23Z");
        verify(logger).info("Event catchup for subscription 'EVENT_LISTENER' caught up 23 events");
        verify(logger).info("Event catchup for subscription 'EVENT_LISTENER' took 5000 milliseconds");

        verify(catchupCompletedEventFirer).fire(new CatchupCompletedEvent(allCatchupsCompletedAt));
        verify(logger).info("Event catchup completed at 2019-02-23T17:12:46Z");
    }
}
