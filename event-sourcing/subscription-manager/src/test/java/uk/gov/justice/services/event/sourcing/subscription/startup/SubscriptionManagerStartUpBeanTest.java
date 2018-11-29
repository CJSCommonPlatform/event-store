package uk.gov.justice.services.event.sourcing.subscription.startup;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.SubscriptionNameQualifier;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Instance;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionManagerStartUpBeanTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Instance<SubscriptionManager> subscriptionManagerBeans;

    @Mock
    private SubscriptionsDescriptorsRegistry descriptorRegistry;

    @Mock
    private SubscriptionsDescriptor descriptor;

    @Mock
    private ManagedExecutorService managedExecutorService;

    @InjectMocks
    private SubscriptionManagerStartUpBean subscriptionManagerStartUpBean;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldStartSubscription() {

        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);

        when(descriptorRegistry.subscriptionsDescriptors()).thenReturn(Sets.newHashSet(descriptor));
        when(descriptor.getSubscriptions()).thenReturn(asList(mock(Subscription.class)));
        when(subscriptionManagerBeans.select(any(SubscriptionNameQualifier.class)).get()).thenReturn(subscriptionManager);

        subscriptionManagerStartUpBean.start();

        verify(managedExecutorService).execute(new StartSubscriptionTask(subscriptionManager));
    }

    @Test
    public void shouldStartAllAvailableSubscription() {

        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);

        final SubscriptionManager subscriptionManager_1 = mock(SubscriptionManager.class);
        final SubscriptionManager subscriptionManager_2 = mock(SubscriptionManager.class);

        when(descriptorRegistry.subscriptionsDescriptors()).thenReturn(Sets.newHashSet(descriptor));
        when(descriptor.getSubscriptions()).thenReturn(asList(subscription_1, subscription_2));

        when(subscriptionManagerBeans.select(any(SubscriptionNameQualifier.class)).get())
                .thenReturn(subscriptionManager_1)
                .thenReturn(subscriptionManager_2);

        subscriptionManagerStartUpBean.start();

        verify(managedExecutorService).execute(new StartSubscriptionTask(subscriptionManager_1));
        verify(managedExecutorService).execute(new StartSubscriptionTask(subscriptionManager_2));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDoNothingWhenNoSubscriptionsExist() {

        when(descriptorRegistry.subscriptionsDescriptors()).thenReturn(Sets.newHashSet(descriptor));
        when(descriptor.getSubscriptions()).thenReturn(EMPTY_LIST);

        subscriptionManagerStartUpBean.start();

        verifyZeroInteractions(subscriptionManagerBeans);
    }
}
