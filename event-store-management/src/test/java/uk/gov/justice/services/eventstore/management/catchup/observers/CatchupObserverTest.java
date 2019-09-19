package uk.gov.justice.services.eventstore.management.catchup.observers;

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
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupCompletedEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupStartedEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupDurationCalculator;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupsInProgressCache;
import uk.gov.justice.services.eventstore.management.catchup.process.EventCatchupRunner;
import uk.gov.justice.services.eventstore.management.logging.MdcLogger;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

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
    private EventCatchupRunner eventCatchupRunner;

    @Mock
    private CatchupsInProgressCache catchupsInProgressCache;

    @Mock
    private CatchupDurationCalculator catchupDurationCalculator;

    @Mock
    private Event<CatchupCompletedEvent> catchupCompletedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private MdcLogger mdcLogger;

    @Mock
    private Logger logger;

    @InjectMocks
    private CatchupObserver catchupObserver;

    @Captor
    private ArgumentCaptor<CatchupInProgress> catchupInProgressCaptor;

    private Consumer<Runnable> testConsumer = Runnable::run;

    @Test
    public void shouldCallTheCatchupRunnerOnCatchupRequested() throws Exception {

        final CatchupRequestedEvent catchupRequestedEvent = mock(CatchupRequestedEvent.class);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);

        catchupObserver.onCatchupRequested(catchupRequestedEvent);

        verify(logger).info("Event catchup requested");
        verify(eventCatchupRunner).runEventCatchup(catchupRequestedEvent);
    }

    @Test
    public void shouldClearEventsInProgressOnCatchupStart() throws Exception {

        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);

        catchupObserver.onCatchupStarted(new CatchupStartedEvent(catchupStartedAt));

        verify(logger).info("Event catchup started at 2019-02-23T17:12:23Z");
        verify(logger).info("Performing catchup of events...");
        verify(catchupsInProgressCache).removeAll();
    }

    @Test
    public void shouldLogCatchupStartedForSubscriptionAndStoreProgress() throws Exception {

        final String subscriptionName = "mySubscription";
        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent = new CatchupStartedForSubscriptionEvent(
                subscriptionName,
                catchupStartedAt);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);

        catchupObserver.onCatchupStartedForSubscription(catchupStartedForSubscriptionEvent);

        verify(catchupsInProgressCache).addCatchupInProgress(catchupInProgressCaptor.capture());
        verify(logger).info("Event catchup for subscription 'mySubscription' started at 2019-02-23T17:12:23Z");

        final CatchupInProgress catchupInProgress = catchupInProgressCaptor.getValue();

        assertThat(catchupInProgress.getSubscriptionName(), is(subscriptionName));
        assertThat(catchupInProgress.getStartedAt(), is(catchupStartedAt));
    }

    @Test
    public void shouldRemoveTheCatchupForSubscriptionInProgressOnCatchupForSubscriptionComplete() throws Exception {


        final String subscriptionName = "mySubscription";
        final String eventSourceName = "myEventSource";
        final String componentName = "EVENT_LISTENER";

        final ZonedDateTime catchupCompletedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final int totalNumberOfEvents = 23;
        final SystemCommand target = mock(SystemCommand.class);

        final Duration catchupDuration = Duration.of(5_000, MILLIS);

        final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = new CatchupCompletedForSubscriptionEvent(
                subscriptionName,
                eventSourceName,
                componentName,
                target,
                catchupCompletedAt,
                totalNumberOfEvents
        );

        final CatchupInProgress catchupInProgress = mock(CatchupInProgress.class);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);
        when(catchupsInProgressCache.removeCatchupInProgress(subscriptionName)).thenReturn(catchupInProgress);
        when(catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent)).thenReturn(catchupDuration);

        when(catchupsInProgressCache.noCatchupsInProgress()).thenReturn(false);

        catchupObserver.onCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

        verify(logger).info("Event catchup for subscription 'mySubscription' completed at 2019-02-23T17:12:23Z");
        verify(logger).info("Event catchup for subscription 'mySubscription' caught up 23 events");
        verify(logger).info("Event catchup for subscription 'mySubscription' took 5000 milliseconds");

        verifyZeroInteractions(catchupCompletedEventFirer);
    }

    @Test
    public void shouldFireTheCatchupCompleteEventIfAllCatchupsForSubscriptionsComplete() throws Exception {

        final String subscriptionName = "mySubscription";
        final String eventSourceName = "myEventSource";
        final String componentName = "EVENT_LISTENER";
        final ZonedDateTime catchupCompletedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final ZonedDateTime allCatchupsCompletedAt = catchupCompletedAt.plusSeconds(23);
        final int totalNumberOfEvents = 23;
        final SystemCommand target = mock(SystemCommand.class);

        final Duration catchupDuration = Duration.of(5_000, MILLIS);

        final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = new CatchupCompletedForSubscriptionEvent(
                subscriptionName,
                eventSourceName,
                componentName,
                target,
                catchupCompletedAt,
                totalNumberOfEvents
        );

        final CatchupInProgress catchupInProgress = mock(CatchupInProgress.class);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);
        when(catchupsInProgressCache.removeCatchupInProgress(subscriptionName)).thenReturn(catchupInProgress);
        when(catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent)).thenReturn(catchupDuration);

        when(catchupsInProgressCache.noCatchupsInProgress()).thenReturn(true);
        when(clock.now()).thenReturn(allCatchupsCompletedAt);

        catchupObserver.onCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

        verify(logger).info("Event catchup for subscription 'mySubscription' completed at 2019-02-23T17:12:23Z");
        verify(logger).info("Event catchup for subscription 'mySubscription' caught up 23 events");
        verify(logger).info("Event catchup for subscription 'mySubscription' took 5000 milliseconds");

        verify(catchupCompletedEventFirer).fire(new CatchupCompletedEvent(target, allCatchupsCompletedAt));
        verify(logger).info("Event catchup fully complete at 2019-02-23T17:12:46Z");
    }
}
