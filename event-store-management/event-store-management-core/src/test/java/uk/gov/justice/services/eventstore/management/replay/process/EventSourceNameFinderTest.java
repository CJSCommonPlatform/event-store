package uk.gov.justice.services.eventstore.management.replay.process;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.eventstore.management.catchup.process.PriorityComparatorProvider;
import uk.gov.justice.subscription.domain.builders.SubscriptionBuilder;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.subscription.domain.builders.SubscriptionsDescriptorBuilder.subscriptionsDescriptor;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceNameFinderTest {

    @Mock
    private SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    private PriorityComparatorProvider priorityComparatorProvider;

    private EventSourceNameFinder eventSourceNameFinder;

    @Before
    public void setup() {
        priorityComparatorProvider = new PriorityComparatorProvider();
        eventSourceNameFinder = new EventSourceNameFinder(subscriptionsDescriptorsRegistry, priorityComparatorProvider);
    }

    @Test
    public void shouldFilterEventListenerSubscriptionDescriptorAndFetchEventSourceNameFromSortedSubscriptions() {
        final SubscriptionsDescriptor eventListenerSD = subscriptionsDescriptor()
                .withServiceComponent(EVENT_LISTENER)
                .withSubscriptions(asList(
                        SubscriptionBuilder.subscription()
                                .withPrioritisation(2)
                                .withEventSourceName("listenerEventSourceName2")
                                .build(),
                        SubscriptionBuilder.subscription()
                                .withPrioritisation(1)
                                .withEventSourceName("listenerEventSourceName1")
                                .build()
                ))
                .build();
        final SubscriptionsDescriptor eventProcessorSD = subscriptionsDescriptor()
                .withServiceComponent(EVENT_PROCESSOR)
                .withSubscriptions(asList(
                        SubscriptionBuilder.subscription()
                                .withPrioritisation(2)
                                .withEventSourceName("processorEventSourceName2")
                                .build(),
                        SubscriptionBuilder.subscription()
                                .withPrioritisation(1)
                                .withEventSourceName("processorEventSourceName1")
                                .build()
                ))
                .build();
        when(subscriptionsDescriptorsRegistry.getAll()).thenReturn(asList(eventListenerSD, eventProcessorSD));

        final String eventSourceName = eventSourceNameFinder.getEventSourceNameOfEventListener();

        assertThat(eventSourceName, is("listenerEventSourceName1"));
    }

    @Test
    public void shouldThrowExceptionWhenSubscriptionsNotFoundForEventListenerSubscriptionDescriptor() {
        final SubscriptionsDescriptor eventListenerSD = subscriptionsDescriptor()
                .withServiceComponent(EVENT_LISTENER)
                .withSubscriptions(emptyList())
                .build();
        when(subscriptionsDescriptorsRegistry.getAll()).thenReturn(singletonList(eventListenerSD));

        final ReplayEventFailedException e = assertThrows(ReplayEventFailedException.class, () -> eventSourceNameFinder.getEventSourceNameOfEventListener());

        assertThat(e.getMessage(), is("No event source name found for event listener"));
    }

    @Test
    public void shouldThrowExceptionWhenEventListenerSubscriptionDescriptorNotFound() {
        final SubscriptionsDescriptor eventProcessorSD = subscriptionsDescriptor()
                .withServiceComponent(EVENT_PROCESSOR)
                .withSubscriptions(singletonList(
                        SubscriptionBuilder.subscription()
                                .withPrioritisation(2)
                                .withEventSourceName("processorEventSourceName2")
                                .build()
                ))
                .build();
        when(subscriptionsDescriptorsRegistry.getAll()).thenReturn(singletonList(eventProcessorSD));

        final ReplayEventFailedException e = assertThrows(ReplayEventFailedException.class, () -> eventSourceNameFinder.getEventSourceNameOfEventListener());

        assertThat(e.getMessage(), is("No event source name found for event listener"));
    }
}