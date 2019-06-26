package uk.gov.justice.services.eventstore.management.indexer.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.catchup.task.EventStreamConsumerManagerFactory;
import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventIndexerCatchupProcessorFactoryTest {

    @Mock
    private ProcessedEventTrackingService processedEventTrackingService;

    @Mock
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Mock
    private Event<IndexerCatchupStartedForSubscriptionEvent> indexerCatchupStartedForSubscriptionEventFirer;

    @Mock
    private Event<IndexerCatchupCompletedForSubscriptionEvent> indexerCatchuCompletedForSubscriptionEventFirer;

    @Mock
    private EventStreamConsumerManagerFactory eventStreamConsumerManagerFactory;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private EventIndexerCatchupProcessorFactory eventCatchupProcessorFactory;

    @Test
    public void shouldCreateEventCatchupProcessorFactory() throws Exception {

        final EventStreamConsumerManager eventStreamConsumerManager = mock(EventStreamConsumerManager.class);
        when(eventStreamConsumerManagerFactory.create()).thenReturn(eventStreamConsumerManager);

        final EventIndexerCatchupProcessor eventIndexerCatchupProcessor = eventCatchupProcessorFactory.create();

        assertThat(getValueOfField(eventIndexerCatchupProcessor, "processedEventTrackingService", ProcessedEventTrackingService.class), is(processedEventTrackingService));
        assertThat(getValueOfField(eventIndexerCatchupProcessor, "publishedEventSourceProvider", PublishedEventSourceProvider.class), is(publishedEventSourceProvider));
        assertThat(getValueOfField(eventIndexerCatchupProcessor, "indexerCatchupStartedForSubscriptionEventFirer", Event.class), is(indexerCatchupStartedForSubscriptionEventFirer));
        assertThat(getValueOfField(eventIndexerCatchupProcessor, "indexerCatchupCompletedForSubscriptionEventFirer", Event.class), is(indexerCatchuCompletedForSubscriptionEventFirer));
        assertThat(getValueOfField(eventIndexerCatchupProcessor, "eventStreamConsumerManager", EventStreamConsumerManager.class), is(eventStreamConsumerManager));
        assertThat(getValueOfField(eventIndexerCatchupProcessor, "clock", UtcClock.class), is(clock));
    }
}
