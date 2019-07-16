package uk.gov.justice.services.eventstore.management.indexer.observers;

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
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupCompletedEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupStartedEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.indexer.process.EventIndexerCatchupRunner;
import uk.gov.justice.services.eventstore.management.indexer.process.IndexerCatchupDurationCalculator;
import uk.gov.justice.services.eventstore.management.indexer.process.IndexerCatchupInProgress;
import uk.gov.justice.services.eventstore.management.indexer.process.IndexerCatchupsInProgressCache;
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
public class IndexerCatchupObserverTest {
    @Mock
    private Logger logger;

    @Mock
    private EventIndexerCatchupRunner eventIndexerCatchupRunner;

    @Mock
    private IndexerCatchupsInProgressCache indexerCatchupsInProgressCache;

    @Mock
    private IndexerCatchupDurationCalculator indexerCatchupDurationCalculator;

    @Mock
    private Event<IndexerCatchupCompletedEvent> indexerCatchupCompletedEventEvent;

    @Mock
    private UtcClock clock;

    @Captor
    private ArgumentCaptor<IndexerCatchupInProgress> indexerCatchupInProgressArgumentCaptor;

    @InjectMocks
    private IndexerCatchupObserver indexerCatchupObserver;

    @Test
    public void shouldCallTheIndexerCatchupRunnerOnCatchupRequested() throws Exception {

        final IndexerCatchupRequestedEvent indexerCatchupRequestedEvent = mock(IndexerCatchupRequestedEvent.class);
        indexerCatchupObserver.onIndexerCatchupRequested(indexerCatchupRequestedEvent);

        verify(logger).info("Event indexer catchup requested");
        verify(eventIndexerCatchupRunner).runEventIndexerCatchup(indexerCatchupRequestedEvent);
    }

    @Test
    public void shouldClearEventsInProgressOnIndexerCatchupStart() throws Exception {

        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        indexerCatchupObserver.onIndexerCatchupStarted(new IndexerCatchupStartedEvent(catchupStartedAt));

        verify(logger).info("Event indexer catchup started at 2019-02-23T17:12:23Z");
        verify(logger).info("Performing indexer catchup of events...");
        verify(indexerCatchupsInProgressCache).removeAll();
    }

    @Test
    public void shouldLogIndexerCatchupStartedForSubscriptionAndStoreProgress() throws Exception {

        final String subscriptionName = "mySubscription";
        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        final IndexerCatchupStartedForSubscriptionEvent indexerCatchupStartedForSubscriptionEvent = new IndexerCatchupStartedForSubscriptionEvent(
                subscriptionName,
                catchupStartedAt);

        indexerCatchupObserver.onIndexerCatchupStartedForSubscription(indexerCatchupStartedForSubscriptionEvent);

        verify(indexerCatchupsInProgressCache).addCatchupInProgress(indexerCatchupInProgressArgumentCaptor.capture());
        verify(logger).info("Event indexer catchup for subscription 'mySubscription' started at 2019-02-23T17:12:23Z");

        final IndexerCatchupInProgress catchupInProgress = indexerCatchupInProgressArgumentCaptor.getValue();

        assertThat(catchupInProgress.getSubscriptionName(), is(subscriptionName));
        assertThat(catchupInProgress.getStartedAt(), is(catchupStartedAt));
    }

    @Test
    public void shouldRemoveTheCatchupForSubscriptionInProgressOnCatchupForSubscriptionComplete() throws Exception {


        final String subscriptionName = "mySubscription";
        final String eventSourceName = "myEventSource";
        final String componentName = "EVENT_INDEXER";

        final ZonedDateTime catchupCompletedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final int totalNumberOfEvents = 23;
        final SystemCommand target = mock(SystemCommand.class);

        final Duration catchupDuration = Duration.of(5_000, MILLIS);

        final IndexerCatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = new IndexerCatchupCompletedForSubscriptionEvent(
                subscriptionName,
                eventSourceName,
                componentName,
                target,
                catchupCompletedAt,
                totalNumberOfEvents
        );

        final IndexerCatchupInProgress catchupInProgress = mock(IndexerCatchupInProgress.class);

        when(indexerCatchupsInProgressCache.removeCatchupInProgress(subscriptionName)).thenReturn(catchupInProgress);
        when(indexerCatchupDurationCalculator.calculate(catchupInProgress, catchupCompletedForSubscriptionEvent)).thenReturn(catchupDuration);

        when(indexerCatchupsInProgressCache.noCatchupsInProgress()).thenReturn(false);

        indexerCatchupObserver.onIndexerCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

        verify(logger).info("Event indexer catchup for subscription 'mySubscription' completed at 2019-02-23T17:12:23Z");
        verify(logger).info("Event indexer catchup for subscription 'mySubscription' caught up 23 events");
        verify(logger).info("Event indexer catchup for subscription 'mySubscription' took 5000 milliseconds");

        verifyZeroInteractions(indexerCatchupCompletedEventEvent);
    }

    @Test
    public void shouldFireTheCatchupCompleteEventIfAllCatchupsForSubscriptionsComplete() throws Exception {

        final String subscriptionName = "mySubscription";
        final String eventSourceName = "myEventSource";
        final String componentName = "EVENT_INDEXER";
        final ZonedDateTime catchupCompletedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final ZonedDateTime allCatchupsCompletedAt = catchupCompletedAt.plusSeconds(23);
        final int totalNumberOfEvents = 23;
        final SystemCommand target = mock(SystemCommand.class);

        final Duration catchupDuration = Duration.of(5_000, MILLIS);

        final IndexerCatchupCompletedForSubscriptionEvent indexerCatchupCompletedForSubscriptionEvent = new IndexerCatchupCompletedForSubscriptionEvent(
                subscriptionName,
                eventSourceName,
                componentName,
                target,
                catchupCompletedAt,
                totalNumberOfEvents
        );

        final IndexerCatchupInProgress indexerCatchupInProgress = mock(IndexerCatchupInProgress.class);

        when(indexerCatchupsInProgressCache.removeCatchupInProgress(subscriptionName)).thenReturn(indexerCatchupInProgress);
        when(indexerCatchupDurationCalculator.calculate(indexerCatchupInProgress, indexerCatchupCompletedForSubscriptionEvent)).thenReturn(catchupDuration);

        when(indexerCatchupsInProgressCache.noCatchupsInProgress()).thenReturn(true);
        when(clock.now()).thenReturn(allCatchupsCompletedAt);

        indexerCatchupObserver.onIndexerCatchupCompleteForSubscription(indexerCatchupCompletedForSubscriptionEvent);

        verify(logger).info("Event indexer catchup for subscription 'mySubscription' completed at 2019-02-23T17:12:23Z");
        verify(logger).info("Event indexer catchup for subscription 'mySubscription' caught up 23 events");
        verify(logger).info("Event indexer catchup for subscription 'mySubscription' took 5000 milliseconds");

        verify(indexerCatchupCompletedEventEvent).fire(new IndexerCatchupCompletedEvent(target, allCatchupsCompletedAt));
        verify(logger).info("Event indexer catchup fully complete at 2019-02-23T17:12:46Z");
    }

}
