package uk.gov.justice.services.eventstore.management.catchup.observers;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.EVENT_CATCHUP;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupDurationCalculator;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;
import uk.gov.justice.services.eventstore.management.catchup.process.EventCatchupRunner;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupError;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupErrorStateManager;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupStateManager;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupProcessingOfEventFailedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class CatchupLifecycleTest {

    @Mock
    private EventCatchupRunner eventCatchupRunner;

    @Mock
    private CatchupStateManager catchupStateManager;

    @Mock
    private CatchupErrorStateManager catchupErrorStateManager;

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

    @Captor
    private ArgumentCaptor<CatchupError> catchupErrorCaptor;

    @InjectMocks
    private CatchupLifecycle catchupLifecycle;

    @Test
    public void shouldCallTheCatchupRunnerOnCatchupRequested() throws Exception {

        final UUID commandId = randomUUID();
        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final CatchupType catchupType = EVENT_CATCHUP;

        final CatchupCommand catchupCommand = new CatchupCommand();
        final CatchupRequestedEvent catchupRequestedEvent = new CatchupRequestedEvent(
                commandId,
                catchupType,
                catchupCommand,
                catchupStartedAt
        );

        catchupLifecycle.handleCatchupRequested(catchupRequestedEvent);

        verify(logger).info("Event catchup requested");

        final InOrder inOrder = inOrder(
                catchupStateManager,
                catchupErrorStateManager,
                eventCatchupRunner);

        inOrder.verify(catchupStateManager).clear(catchupType);
        inOrder.verify(catchupErrorStateManager).clear(catchupType);
        inOrder.verify(eventCatchupRunner).runEventCatchup(commandId, catchupType, catchupCommand);
    }

    @Test
    public void shouldHanldeCatchupStarted() throws Exception {

        final UUID commandId = randomUUID();
        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        catchupLifecycle.handleCatchupStarted(new CatchupStartedEvent(commandId, EVENT_CATCHUP, catchupStartedAt));

        verify(logger).info("Event catchup started at 2019-02-23T17:12:23Z");
    }

    @Test
    public void shouldLogCatchupStartedForSubscriptionAndStoreProgress() throws Exception {

        final UUID commandId = randomUUID();
        final String subscriptionName = "mySubscription";
        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent = new CatchupStartedForSubscriptionEvent(
                commandId,
                subscriptionName,
                EVENT_CATCHUP,
                catchupStartedAt);
        catchupLifecycle.handleCatchupStartedForSubscription(catchupStartedForSubscriptionEvent);

        verify(catchupStateManager).addCatchupInProgress(catchupInProgressCaptor.capture(), catchupTypeCaptor.capture());
        verify(logger).info("Event catchup for subscription 'mySubscription' started at 2019-02-23T17:12:23Z");

        final CatchupInProgress catchupInProgress = catchupInProgressCaptor.getValue();

        assertThat(catchupInProgress.getSubscriptionName(), is(subscriptionName));
        assertThat(catchupInProgress.getStartedAt(), is(catchupStartedAt));

        assertThat(catchupTypeCaptor.getValue(), is(EVENT_CATCHUP));
    }

    @Test
    public void shouldRemoveTheCatchupForSubscriptionInProgressOnCatchupForSubscriptionComplete() throws Exception {

        final UUID commandId = randomUUID();
        final String subscriptionName = "mySubscription";
        final String eventSourceName = "myEventSource";
        final String componentName = "EVENT_LISTENER";

        final ZonedDateTime catchupCompletedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final int totalNumberOfEvents = 23;
        final SystemCommand target = mock(SystemCommand.class);

        final Duration catchupDuration = Duration.of(5_000, MILLIS);

        final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = new CatchupCompletedForSubscriptionEvent(
                commandId,
                EVENT_CATCHUP,
                subscriptionName,
                eventSourceName,
                componentName,
                target,
                catchupCompletedAt,
                totalNumberOfEvents
        );

        final CatchupInProgress catchupInProgress = mock(CatchupInProgress.class);

        when(catchupStateManager.removeCatchupInProgress(subscriptionName, EVENT_CATCHUP)).thenReturn(catchupInProgress);
        when(catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent)).thenReturn(catchupDuration);

        when(catchupStateManager.noCatchupsInProgress(EVENT_CATCHUP)).thenReturn(false);

        catchupLifecycle.handleCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

        verify(logger).info("Event catchup for subscription 'mySubscription' completed at 2019-02-23T17:12:23Z");
        verify(logger).info("Event catchup for subscription 'mySubscription' caught up 23 events");
        verify(logger).info("Event catchup for subscription 'mySubscription' took 5000 milliseconds");

        verifyZeroInteractions(catchupCompletedEventFirer);
    }

    @Test
    public void shouldFireTheCatchupCompleteEventIfAllCatchupsForSubscriptionsComplete() throws Exception {

        final UUID commandId = randomUUID();
        final String subscriptionName = "mySubscription";
        final String eventSourceName = "myEventSource";
        final String componentName = "EVENT_LISTENER";
        final ZonedDateTime catchupCompletedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final ZonedDateTime allCatchupsCompletedAt = catchupCompletedAt.plusSeconds(23);
        final int totalNumberOfEvents = 23;
        final SystemCommand target = mock(SystemCommand.class);

        final Duration catchupDuration = Duration.of(5_000, MILLIS);

        final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = new CatchupCompletedForSubscriptionEvent(
                commandId,
                EVENT_CATCHUP,
                subscriptionName,
                eventSourceName,
                componentName,
                target,
                catchupCompletedAt,
                totalNumberOfEvents
        );

        final CatchupInProgress catchupInProgress = mock(CatchupInProgress.class);

        when(catchupStateManager.removeCatchupInProgress(subscriptionName, EVENT_CATCHUP)).thenReturn(catchupInProgress);
        when(catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent)).thenReturn(catchupDuration);

        when(catchupStateManager.noCatchupsInProgress(EVENT_CATCHUP)).thenReturn(true);
        when(clock.now()).thenReturn(allCatchupsCompletedAt);

        catchupLifecycle.handleCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

        verify(logger).info("Event catchup for subscription 'mySubscription' completed at 2019-02-23T17:12:23Z");
        verify(logger).info("Event catchup for subscription 'mySubscription' caught up 23 events");
        verify(logger).info("Event catchup for subscription 'mySubscription' took 5000 milliseconds");

        verify(catchupCompletedEventFirer).fire(new CatchupCompletedEvent(commandId, target, allCatchupsCompletedAt, EVENT_CATCHUP));
    }

    @Test
    public void shouldHandleCatchupFailureCompletion() throws Exception {

        final UUID commandId = randomUUID();
        final ZonedDateTime catchupCompletedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final CatchupCommand target = new CatchupCommand();
        final CatchupType catchupType = EVENT_CATCHUP;
        final CatchupCompletedEvent catchupCompletedEvent = new CatchupCompletedEvent(
                commandId,
                target,
                catchupCompletedAt,
                catchupType
        );

        when(catchupErrorStateManager.getErrors(catchupType)).thenReturn(emptyList());

        catchupLifecycle.handleCatchupComplete(catchupCompletedEvent);

        verify(logger).info("Event catchup successfully completed with 0 errors at 2019-02-23T17:12:23Z");
    }

    @Test
    public void shouldHandleCatchupSuccessfulCompletion() throws Exception {

        final UUID commandId = randomUUID();
        final List<CatchupError> catchupErrors = asList(
                mock(CatchupError.class),
                mock(CatchupError.class),
                mock(CatchupError.class));

        final ZonedDateTime catchupCompletedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final CatchupCommand target = new CatchupCommand();
        final CatchupType catchupType = EVENT_CATCHUP;
        final CatchupCompletedEvent catchupCompletedEvent = new CatchupCompletedEvent(
                commandId,
                target,
                catchupCompletedAt,
                catchupType
        );

        when(catchupErrorStateManager.getErrors(catchupType)).thenReturn(catchupErrors);

        catchupLifecycle.handleCatchupComplete(catchupCompletedEvent);

        verify(logger).error("Event catchup failed with 3 errors");
    }

    @Test
    public void shouldHandleCatchupProcessingOfEventFailed() throws Exception {

        final UUID commandId = randomUUID();
        final UUID eventId = randomUUID();
        final String metadata = "{some: metadata}";
        final NullPointerException exception = new NullPointerException("Ooops");
        final CatchupType catchupType = EVENT_CATCHUP;
        final String subscriptionName = "subscriptionName";

        final CatchupProcessingOfEventFailedEvent catchupProcessingOfEventFailedEvent = new CatchupProcessingOfEventFailedEvent(
                commandId,
                eventId,
                metadata,
                exception,
                catchupType,
                subscriptionName
        );

        catchupLifecycle.handleCatchupProcessingOfEventFailed(catchupProcessingOfEventFailedEvent);

        verify(catchupErrorStateManager).add(catchupErrorCaptor.capture(), eq(catchupType));

        final CatchupError catchupError = catchupErrorCaptor.getValue();

        assertThat(catchupError.getEventId(), is(eventId));
        assertThat(catchupError.getException(), is(exception));
        assertThat(catchupError.getMetadata(), is(metadata));
        assertThat(catchupError.getCatchupType(), is(catchupType));
        assertThat(catchupError.getSubscriptionName(), is(subscriptionName));
    }
}
