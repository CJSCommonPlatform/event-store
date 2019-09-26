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
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.EVENT_CATCHUP;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupDurationCalculator;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupsInProgressCache;
import uk.gov.justice.services.eventstore.management.catchup.process.EventCatchupRunner;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

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
public class CatchupLifecycleTest {

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
    private Logger logger;

    @Captor
    private ArgumentCaptor<CatchupInProgress> catchupInProgressCaptor;

    @Captor
    private ArgumentCaptor<CatchupType> catchupTypeCaptor;

    @InjectMocks
    private CatchupLifecycle catchupLifecycle;

    @Test
    public void shouldCallTheCatchupRunnerOnCatchupRequested() throws Exception {

        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        final CatchupCommand catchupCommand = new CatchupCommand();
        final CatchupRequestedEvent catchupRequestedEvent = new CatchupRequestedEvent(
                EVENT_CATCHUP,
                catchupCommand,
                catchupStartedAt
        );

        catchupLifecycle.handleCatchupRequested(catchupRequestedEvent);

        verify(logger).info("Event catchup requested");
        verify(eventCatchupRunner).runEventCatchup(EVENT_CATCHUP, catchupCommand);
    }

    @Test
    public void shouldClearEventsInProgressOnCatchupStart() throws Exception {

        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        catchupLifecycle.handleCatchupStarted(new CatchupStartedEvent(EVENT_CATCHUP, catchupStartedAt));

        verify(logger).info("Event catchup started at 2019-02-23T17:12:23Z");
        verify(catchupsInProgressCache).removeAll(EVENT_CATCHUP);
    }

    @Test
    public void shouldLogCatchupStartedForSubscriptionAndStoreProgress() throws Exception {

        final String subscriptionName = "mySubscription";
        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent = new CatchupStartedForSubscriptionEvent(
                subscriptionName,
                EVENT_CATCHUP,
                catchupStartedAt);
        catchupLifecycle.handleCatchupStartedForSubscription(catchupStartedForSubscriptionEvent);

        verify(catchupsInProgressCache).addCatchupInProgress(catchupInProgressCaptor.capture(), catchupTypeCaptor.capture());
        verify(logger).info("Event catchup for subscription 'mySubscription' started at 2019-02-23T17:12:23Z");

        final CatchupInProgress catchupInProgress = catchupInProgressCaptor.getValue();

        assertThat(catchupInProgress.getSubscriptionName(), is(subscriptionName));
        assertThat(catchupInProgress.getStartedAt(), is(catchupStartedAt));

        assertThat(catchupTypeCaptor.getValue(), is(EVENT_CATCHUP));
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
                EVENT_CATCHUP,
                subscriptionName,
                eventSourceName,
                componentName,
                target,
                catchupCompletedAt,
                totalNumberOfEvents
        );

        final CatchupInProgress catchupInProgress = mock(CatchupInProgress.class);

        when(catchupsInProgressCache.removeCatchupInProgress(subscriptionName, EVENT_CATCHUP)).thenReturn(catchupInProgress);
        when(catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent)).thenReturn(catchupDuration);

        when(catchupsInProgressCache.noCatchupsInProgress(EVENT_CATCHUP)).thenReturn(false);

        catchupLifecycle.handleCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

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
                EVENT_CATCHUP,
                subscriptionName,
                eventSourceName,
                componentName,
                target,
                catchupCompletedAt,
                totalNumberOfEvents
        );

        final CatchupInProgress catchupInProgress = mock(CatchupInProgress.class);

        when(catchupsInProgressCache.removeCatchupInProgress(subscriptionName, EVENT_CATCHUP)).thenReturn(catchupInProgress);
        when(catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent)).thenReturn(catchupDuration);

        when(catchupsInProgressCache.noCatchupsInProgress(EVENT_CATCHUP)).thenReturn(true);
        when(clock.now()).thenReturn(allCatchupsCompletedAt);

        catchupLifecycle.handleCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

        verify(logger).info("Event catchup for subscription 'mySubscription' completed at 2019-02-23T17:12:23Z");
        verify(logger).info("Event catchup for subscription 'mySubscription' caught up 23 events");
        verify(logger).info("Event catchup for subscription 'mySubscription' took 5000 milliseconds");

        verify(catchupCompletedEventFirer).fire(new CatchupCompletedEvent(target, allCatchupsCompletedAt));
        verify(logger).info("Event catchup fully complete at 2019-02-23T17:12:46Z");
    }
}
