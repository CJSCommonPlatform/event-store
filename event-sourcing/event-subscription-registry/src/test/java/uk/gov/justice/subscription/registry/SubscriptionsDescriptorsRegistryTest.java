package uk.gov.justice.subscription.registry;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.subscription.domain.builders.SubscriptionBuilder.subscription;
import static uk.gov.justice.subscription.domain.builders.SubscriptionsDescriptorBuilder.subscriptionsDescriptor;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.List;

import org.junit.jupiter.api.Test;

public class SubscriptionsDescriptorsRegistryTest {

    @Test
    public void shouldGetASubscriptionByName() throws Exception {


        final Subscription subscription_1 = subscription().withName("subscription_1").build();
        final Subscription subscription_2 = subscription().withName("subscription_2").build();
        final Subscription subscription_3 = subscription().withName("subscription_3").build();
        final Subscription subscription_4 = subscription().withName("subscription_4").build();

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = subscriptionsDescriptor()
                .withService("service 1")
                .withSubscription(subscription_1)
                .withSubscription(subscription_2)
                .build();
        final SubscriptionsDescriptor subscriptionsDescriptor_2 = subscriptionsDescriptor()
                .withSubscription(subscription_3)
                .withSubscription(subscription_4)
                .withService("service 2")
                .build();

        final List<SubscriptionsDescriptor> subscriptionsDescriptors = asList(subscriptionsDescriptor_1, subscriptionsDescriptor_2);
        final SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry = new SubscriptionsDescriptorsRegistry(subscriptionsDescriptors);

        assertThat(subscriptionsDescriptorsRegistry.getSubscriptionFor(subscription_3.getName()), is(subscription_3));
    }

    @Test
    public void shouldGetTheComponentNameOfASubscription() throws Exception {

        final String componentName_1 = "component name 1";
        final String componentName_2 = "component name 2";

        final Subscription subscription_1 = subscription().withName("subscription_1").build();
        final Subscription subscription_2 = subscription().withName("subscription_2").build();
        final Subscription subscription_3 = subscription().withName("subscription_3").build();
        final Subscription subscription_4 = subscription().withName("subscription_4").build();

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = subscriptionsDescriptor()
                .withService("service 1")
                .withServiceComponent(componentName_1)
                .withSubscription(subscription_1)
                .withSubscription(subscription_2)
                .build();
        final SubscriptionsDescriptor subscriptionsDescriptor_2 = subscriptionsDescriptor()
                .withSubscription(subscription_3)
                .withSubscription(subscription_4)
                .withService("service 2")
                .withServiceComponent(componentName_2)
                .build();

        final List<SubscriptionsDescriptor> subscriptionsDescriptors = asList(subscriptionsDescriptor_1, subscriptionsDescriptor_2);
        final SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry = new SubscriptionsDescriptorsRegistry(subscriptionsDescriptors);

        assertThat(subscriptionsDescriptorsRegistry.findComponentNameBy(subscription_4.getName()), is(componentName_2));
    }

    @Test
    public void shouldGetAllSubscriptioDescriptors() throws Exception {

        final Subscription subscription_1 = subscription().withName("subscription_1").build();
        final Subscription subscription_2 = subscription().withName("subscription_2").build();
        final Subscription subscription_3 = subscription().withName("subscription_3").build();
        final Subscription subscription_4 = subscription().withName("subscription_4").build();

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = subscriptionsDescriptor()
                .withService("service 1")
                .withSubscription(subscription_1)
                .withSubscription(subscription_2)
                .build();
        final SubscriptionsDescriptor subscriptionsDescriptor_2 = subscriptionsDescriptor()
                .withSubscription(subscription_3)
                .withSubscription(subscription_4)
                .withService("service 2")
                .build();

        final List<SubscriptionsDescriptor> subscriptionsDescriptors = asList(subscriptionsDescriptor_1, subscriptionsDescriptor_2);
        final SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry = new SubscriptionsDescriptorsRegistry(subscriptionsDescriptors);


        assertThat(subscriptionsDescriptorsRegistry.getAll().size(), is(2));
        assertThat(subscriptionsDescriptorsRegistry.getAll(), hasItems(subscriptionsDescriptor_1, subscriptionsDescriptor_2));
    }
}
