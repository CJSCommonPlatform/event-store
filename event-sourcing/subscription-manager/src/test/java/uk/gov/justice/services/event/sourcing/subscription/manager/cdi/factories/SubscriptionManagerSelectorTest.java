package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.BackwardsCompatibleSubscriptionManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.DefaultSubscriptionManager;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionManagerSelectorTest {

    @Mock
    private SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry;

    @Mock
    private DefaultSubscriptionManagerFactory defaultSubscriptionManagerFactory;

    @Mock
    private BackwardsCompatibleSubscriptionManagerFactory backwardsCompatibleSubscriptionManagerFactory;

    @InjectMocks
    private SubscriptionManagerSelector subscriptionManagerSelector;

    @Test
    public void shouldCreateDefaultSubscriptionManagerIfTheComponentIsAnEventListener() throws Exception {

        final String subscriptionName = "subscriptionName";
        final String componentName = "MY_EVENT_LISTENER";

        final Subscription subscription = mock(Subscription.class);

        final DefaultSubscriptionManager defaultSubscriptionManager = mock(DefaultSubscriptionManager.class);

        when(subscription.getName()).thenReturn(subscriptionName);
        when(subscriptionDescriptorRegistry.findComponentNameBy(subscriptionName)).thenReturn(componentName);
        when(defaultSubscriptionManagerFactory.create(componentName)).thenReturn(defaultSubscriptionManager);

        assertThat(subscriptionManagerSelector.selectFor(subscription), is(defaultSubscriptionManager));

        verifyNoInteractions(backwardsCompatibleSubscriptionManagerFactory);
    }

    @Test
    public void shouldCreateDefaultSubscriptionManagerIfTheComponentIsAnEventIndexer() throws Exception {

        final String subscriptionName = "subscriptionName";
        final String componentName = "MY_EVENT_INDEXER";

        final Subscription subscription = mock(Subscription.class);

        final DefaultSubscriptionManager defaultSubscriptionManager = mock(DefaultSubscriptionManager.class);

        when(subscription.getName()).thenReturn(subscriptionName);
        when(subscriptionDescriptorRegistry.findComponentNameBy(subscriptionName)).thenReturn(componentName);
        when(defaultSubscriptionManagerFactory.create(componentName)).thenReturn(defaultSubscriptionManager);

        assertThat(subscriptionManagerSelector.selectFor(subscription), is(defaultSubscriptionManager));

        verifyNoInteractions(backwardsCompatibleSubscriptionManagerFactory);
    }

    @Test
    public void shouldCreateBackwardsCompatibleSubscriptionManagerIfTheComponentIsNotAnEventListener() throws Exception {

        final String subscriptionName = "subscriptionName";
        final String eventSourceName = "eventSourceName";
        final String componentName = "MY_EVENT_PROCESSOR";

        final EventSource eventSource = mock(EventSource.class);
        final Subscription subscription = mock(Subscription.class);

        final InterceptorChainProcessor interceptorChainProcessor = mock(InterceptorChainProcessor.class);
        final BackwardsCompatibleSubscriptionManager backwardsCompatibleSubscriptionManager = mock(BackwardsCompatibleSubscriptionManager.class);

        when(subscription.getName()).thenReturn(subscriptionName);
        when(subscriptionDescriptorRegistry.findComponentNameBy(subscriptionName)).thenReturn(componentName);
        when(backwardsCompatibleSubscriptionManagerFactory.create(componentName)).thenReturn(backwardsCompatibleSubscriptionManager);

        assertThat(subscriptionManagerSelector.selectFor(subscription), is(backwardsCompatibleSubscriptionManager));

        verifyNoInteractions(defaultSubscriptionManagerFactory);
    }
}
