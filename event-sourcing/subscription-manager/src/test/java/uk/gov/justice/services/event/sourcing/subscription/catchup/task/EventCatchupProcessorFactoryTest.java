package uk.gov.justice.services.event.sourcing.subscription.catchup.task;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupProcessorFactoryTest {

    @Mock
    private ProcessedEventTrackingService processedEventTrackingService;

    @Mock
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Mock
    private Event<CatchupStartedForSubscriptionEvent> catchupStartedForSubscriptionEventFirer;

    @Mock
    private Event<CatchupCompletedForSubscriptionEvent> catchupCompletedForSubscriptionEventFirer;

    @Mock
    private EventStreamConsumerManagerFactory eventStreamConsumerManagerFactory;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private EventCatchupProcessorFactory eventCatchupProcessorFactory;

    @Test
    public void shouldCreateEventCatchupProcessorFactory() throws Exception {

        final EventStreamConsumerManager eventStreamConsumerManager = mock(EventStreamConsumerManager.class);
        when(eventStreamConsumerManagerFactory.create()).thenReturn(eventStreamConsumerManager);

        final EventCatchupProcessor eventCatchupProcessor = eventCatchupProcessorFactory.create();

        assertThat(getValueOfField(eventCatchupProcessor, "processedEventTrackingService", ProcessedEventTrackingService.class), is(processedEventTrackingService));
        assertThat(getValueOfField(eventCatchupProcessor, "publishedEventSourceProvider", PublishedEventSourceProvider.class), is(publishedEventSourceProvider));
        assertThat(getValueOfField(eventCatchupProcessor, "catchupStartedForSubscriptionEventFirer", Event.class), is(catchupStartedForSubscriptionEventFirer));
        assertThat(getValueOfField(eventCatchupProcessor, "catchupCompletedForSubscriptionEventFirer", Event.class), is(catchupCompletedForSubscriptionEventFirer));
        assertThat(getValueOfField(eventCatchupProcessor, "eventStreamConsumerManager", EventStreamConsumerManager.class), is(eventStreamConsumerManager));
        assertThat(getValueOfField(eventCatchupProcessor, "clock", UtcClock.class), is(clock));
    }
}
