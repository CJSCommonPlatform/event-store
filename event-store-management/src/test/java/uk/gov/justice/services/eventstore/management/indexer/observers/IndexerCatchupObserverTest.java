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
    private EventIndexerCatchupRunner eventIndexerCatchupRunner;

    @Mock
    private IndexerCatchupsInProgressCache indexerCatchupsInProgressCache;

    @Mock
    private IndexerCatchupDurationCalculator indexerCatchupDurationCalculator;

    @Mock
    private Event<IndexerCatchupCompletedEvent> indexerCatchupCompletedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private IndexerCatchupObserver indexerCatchupObserver;

    @Captor
    private ArgumentCaptor<IndexerCatchupInProgress> indexerCatchupInProgressCaptor;

    @Test
    public void shouldCallTheCatchupRunnerOnCatchupRequested() throws Exception {

        indexerCatchupObserver.onIndexerCatchupRequested(mock(IndexerCatchupRequestedEvent.class));

        verify(logger).info("Event indexer catchup requested");
        verify(eventIndexerCatchupRunner).runEventCatchup();
    }

    @Test
    public void shouldClearEventsInProgressOnCatchupStart() throws Exception {

        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        indexerCatchupObserver.onIndexerCatchupStarted(new IndexerCatchupStartedEvent(catchupStartedAt));

        verify(logger).info("Event indexer catchup started at 2019-02-23T17:12:23Z");
        verify(logger).info("Performing indexer catchup of events...");
        verify(indexerCatchupsInProgressCache).removeAll();
    }

    @Test
    public void shouldLogCatchupStartedForSubscriptionAndStoreProgress() throws Exception {

        final String subscriptionName = "EVENT_LISTENER";
        final ZonedDateTime catchupStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        final IndexerCatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent = new IndexerCatchupStartedForSubscriptionEvent(
                subscriptionName,
                catchupStartedAt);

        indexerCatchupObserver.onIndexerCatchupStartedForSubscription(catchupStartedForSubscriptionEvent);

        verify(indexerCatchupsInProgressCache).addCatchupInProgress(indexerCatchupInProgressCaptor.capture());
        verify(logger).info("Event indexer catchup for subscription 'EVENT_LISTENER' started at 2019-02-23T17:12:23Z");

        final IndexerCatchupInProgress catchupInProgress = indexerCatchupInProgressCaptor.getValue();

        assertThat(catchupInProgress.getSubscriptionName(), is(subscriptionName));
        assertThat(catchupInProgress.getStartedAt(), is(catchupStartedAt));
    }

    @Test
    public void shouldRemoveTheCatchupForSubscriptionInProgressOnCatchupForSubscriptionComplete() throws Exception {


        final String subscriptionName = "EVENT_INDEXER";
        final ZonedDateTime catchupCompletedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final int totalNumberOfEvents = 23;

        final Duration catchupDuration = Duration.of(5_000, MILLIS);

        final IndexerCatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = new IndexerCatchupCompletedForSubscriptionEvent(
                subscriptionName,
                totalNumberOfEvents,
                catchupCompletedAt
        );

        final IndexerCatchupInProgress catchupInProgress = mock(IndexerCatchupInProgress.class);

        when(indexerCatchupsInProgressCache.removeCatchupInProgress(subscriptionName)).thenReturn(catchupInProgress);
        when(indexerCatchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent)).thenReturn(catchupDuration);

        when(indexerCatchupsInProgressCache.noCatchupsInProgress()).thenReturn(false);

        indexerCatchupObserver.onIndexerCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

        verify(logger).info("Event indexer catchup for subscription 'EVENT_INDEXER' completed at 2019-02-23T17:12:23Z");
        verify(logger).info("Event indexer catchup for subscription 'EVENT_INDEXER' caught up 23 events");
        verify(logger).info("Event indexer catchup for subscription 'EVENT_INDEXER' took 5000 milliseconds");

        verifyZeroInteractions(indexerCatchupCompletedEventFirer);
    }

    @Test
    public void shouldFireTheCatchupCompleteEventIfAllCatchupsForSubscriptionsComplete() throws Exception {


        final String subscriptionName = "EVENT_INDEXER";
        final ZonedDateTime catchupCompletedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final ZonedDateTime allCatchupsCompletedAt = catchupCompletedAt.plusSeconds(23);
        final int totalNumberOfEvents = 23;

        final Duration catchupDuration = Duration.of(5_000, MILLIS);

        final IndexerCatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = new IndexerCatchupCompletedForSubscriptionEvent(
                subscriptionName,
                totalNumberOfEvents,
                catchupCompletedAt
        );

        final IndexerCatchupInProgress catchupInProgress = mock(IndexerCatchupInProgress.class);

        when(indexerCatchupsInProgressCache.removeCatchupInProgress(subscriptionName)).thenReturn(catchupInProgress);
        when(indexerCatchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent)).thenReturn(catchupDuration);

        when(indexerCatchupsInProgressCache.noCatchupsInProgress()).thenReturn(true);
        when(clock.now()).thenReturn(allCatchupsCompletedAt);

        indexerCatchupObserver.onIndexerCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

        verify(logger).info("Event indexer catchup for subscription 'EVENT_INDEXER' completed at 2019-02-23T17:12:23Z");
        verify(logger).info("Event indexer catchup for subscription 'EVENT_INDEXER' caught up 23 events");
        verify(logger).info("Event indexer catchup for subscription 'EVENT_INDEXER' took 5000 milliseconds");

        verify(indexerCatchupCompletedEventFirer).fire(new IndexerCatchupCompletedEvent(allCatchupsCompletedAt));
        verify(logger).info("Event indexer catchup completed at 2019-02-23T17:12:46Z");
    }
}
