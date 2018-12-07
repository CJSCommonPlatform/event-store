package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.test.utils.FieldAccessor.getFieldFrom;

import uk.gov.justice.services.event.source.subscriptions.repository.jdbc.SubscriptionsRepository;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventCatchupProcessor;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupProcessorFactoryTest {

    @Mock
    private SubscriptionsRepository subscriptionsRepository;

    @InjectMocks
    private EventCatchupProcessorFactory eventCatchupProcessorFactory;

    @Test
    public void shouldCreateEventCatchupProcessorFactory() throws Exception {

        final Subscription subscription = mock(Subscription.class);
        final EventSource eventSource = mock(EventSource.class);
        final EventBufferProcessor eventBufferProcessor = mock(EventBufferProcessor.class);

        final EventCatchupProcessor eventCatchupProcessor = eventCatchupProcessorFactory.create(subscription, eventSource, eventBufferProcessor);

        assertThat(getFieldFrom(eventCatchupProcessor, "subscription", Subscription.class), is(subscription));
        assertThat(getFieldFrom(eventCatchupProcessor, "subscriptionsRepository", SubscriptionsRepository.class), is(subscriptionsRepository));
        assertThat(getFieldFrom(eventCatchupProcessor, "eventSource", EventSource.class), is(eventSource));
        assertThat(getFieldFrom(eventCatchupProcessor, "eventBufferProcessor", EventBufferProcessor.class), is(eventBufferProcessor));
    }
}
