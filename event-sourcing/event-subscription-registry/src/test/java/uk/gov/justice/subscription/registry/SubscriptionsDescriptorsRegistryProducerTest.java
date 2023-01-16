package uk.gov.justice.subscription.registry;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.subscription.domain.builders.SubscriptionBuilder.subscription;
import static uk.gov.justice.subscription.domain.builders.SubscriptionsDescriptorBuilder.subscriptionsDescriptor;

import uk.gov.justice.subscription.SubscriptionsDescriptorParser;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class SubscriptionsDescriptorsRegistryProducerTest {

    @Mock
    private Logger logger;

    @Mock
    private YamlFileFinder yamlFileFinder;

    @Mock
    private SubscriptionsDescriptorParser subscriptionsDescriptorParser;

    @InjectMocks
    private SubscriptionsDescriptorsRegistryProducer subscriptionsDescriptorsRegistryProducer;

    @Test
    public void shouldCreateRegistryOfAllSubscriptionDescriptorDefinitionsFromTheClasspath() throws Exception {

        final String eventListener = "EVENT_LISTENER";
        final String eventProcessor = "EVENT_PROCESSOR";

        final URL url_1 = new URL("file:/test-1");
        final URL url_2 = new URL("file:/test-2");

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = subscriptionsDescriptor()
                .withServiceComponent(eventListener)
                .build();
        final SubscriptionsDescriptor subscriptionsDescriptor_2 = subscriptionsDescriptor()
                .withServiceComponent(eventProcessor)
                .build();

        final List<URL> pathList = asList(url_1, url_2);

        when(yamlFileFinder.getSubscriptionsDescriptorsPaths()).thenReturn(pathList);
        when(subscriptionsDescriptorParser.getSubscriptionDescriptorsFrom(pathList)).thenReturn(Stream.of(subscriptionsDescriptor_1, subscriptionsDescriptor_2));

        final SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry = subscriptionsDescriptorsRegistryProducer.subscriptionDescriptorRegistry();

        assertThat(subscriptionDescriptorRegistry, is(notNullValue()));
        assertThat(subscriptionDescriptorRegistry.getAll().size(), is(2));
    }

    @Test
    public void shouldCreateSingleRegistryAndReturnSameInstance() throws Exception {
        final String eventListener = "EVENT_LISTENER";
        final String eventProcessor = "EVENT_PROCESSOR";

        final URL url_1 = new URL("file:/test-1");
        final URL url_2 = new URL("file:/test-2");

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = subscriptionsDescriptor()
                .withServiceComponent(eventListener)
                .build();
        final SubscriptionsDescriptor subscriptionsDescriptor_2 = subscriptionsDescriptor()
                .withServiceComponent(eventProcessor)
                .build();

        final List<URL> pathList = asList(url_1, url_2);

        when(yamlFileFinder.getSubscriptionsDescriptorsPaths()).thenReturn(pathList);
        when(subscriptionsDescriptorParser.getSubscriptionDescriptorsFrom(pathList)).thenReturn(Stream.of(subscriptionsDescriptor_1, subscriptionsDescriptor_2));

        final SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry_1 = subscriptionsDescriptorsRegistryProducer.subscriptionDescriptorRegistry();
        final SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry_2 = subscriptionsDescriptorsRegistryProducer.subscriptionDescriptorRegistry();

        assertThat(subscriptionDescriptorRegistry_1, is(sameInstance(subscriptionDescriptorRegistry_2)));
    }

    @Test
    public void shouldLogRegisteredSubscriptionDescriptorDefinitions() throws Exception {

        final String eventListener = "EVENT_LISTENER";
        final String eventProcessor = "EVENT_PROCESSOR";

        final URL url_1 = new URL("file:/test-1");
        final URL url_2 = new URL("file:/test-2");

        final Subscription subscription_1 = subscription()
                .withName("Subscription_1")
                .build();

        final Subscription subscription_2 = subscription()
                .withName("Subscription_2")
                .build();

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = subscriptionsDescriptor()
                .withServiceComponent(eventListener)
                .withSubscription(subscription_1)
                .build();

        final SubscriptionsDescriptor subscriptionsDescriptor_2 = subscriptionsDescriptor()
                .withServiceComponent(eventProcessor)
                .withSubscription(subscription_2)
                .build();

        final List<URL> pathList = asList(url_1, url_2);

        when(yamlFileFinder.getSubscriptionsDescriptorsPaths()).thenReturn(pathList);
        when(subscriptionsDescriptorParser.getSubscriptionDescriptorsFrom(pathList)).thenReturn(Stream.of(subscriptionsDescriptor_1, subscriptionsDescriptor_2));

        subscriptionsDescriptorsRegistryProducer.subscriptionDescriptorRegistry();

        verify(logger).debug("Subscription name in registry : Subscription_1");
        verify(logger).debug("Subscription name in registry : Subscription_2");
    }

    @Test
    public void shouldThrowExceptionIfIOExceptionOccursWhenFindingSubscriptionDescriptorDefinitionResourcesOnTheClasspath() throws Exception {

        when(yamlFileFinder.getSubscriptionsDescriptorsPaths()).thenThrow(new IOException());

        try {
            subscriptionsDescriptorsRegistryProducer.subscriptionDescriptorRegistry();
            fail();
        } catch (final Exception e) {
            assertThat(e, is(instanceOf(RegistryException.class)));
            assertThat(e.getMessage(), is("Failed to find yaml/subscriptions-descriptor.yaml resources on the classpath"));
            assertThat(e.getCause(), is(instanceOf(IOException.class)));
        }
    }

    @Test
    public void shouldCreateSubscriptionsDescriptorsRegistryWithEmptySetAndLogIfNoSubscriptionYamls() throws Exception {

        final SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry = subscriptionsDescriptorsRegistryProducer.subscriptionDescriptorRegistry();

        assertThat(subscriptionsDescriptorsRegistry.getAll().size(), is(0));
        verify(logger).info("Failed to find yaml/subscriptions-descriptor.yaml resources on the classpath");
    }
}
