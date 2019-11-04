package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SchemaIdMappingProviderTest {

    @Mock
    private SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    @InjectMocks
    private SchemaIdMappingProvider schemaIdMappingProvider;

    @Test
    public void shouldMapEventNamesToSchemaIds() throws Exception {

        final Event event_1 = new Event("eventName_1", "schemaUrl_1");
        final Event event_2 = new Event("eventName_2", "schemaUrl_2");
        final Event event_3 = new Event("eventName_3", "schemaUrl_3");
        final Event event_4 = new Event("eventName_4", "schemaUrl_4");
        final Event event_5 = new Event("eventName_5", "schemaUrl_5");
        final Event event_6 = new Event("eventName_6", "schemaUrl_6");
        final Event event_7 = new Event("eventName_7", "schemaUrl_7");
        final Event event_8 = new Event("eventName_8", "schemaUrl_8");

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_2 = mock(SubscriptionsDescriptor.class);

        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);
        final Subscription subscription_3 = mock(Subscription.class);
        final Subscription subscription_4 = mock(Subscription.class);

        when(subscriptionsDescriptor_1.getSubscriptions()).thenReturn(asList(subscription_1, subscription_2));
        when(subscriptionsDescriptor_2.getSubscriptions()).thenReturn(asList(subscription_3, subscription_4));

        when(subscription_1.getEvents()).thenReturn(asList(event_1, event_2));
        when(subscription_2.getEvents()).thenReturn(asList(event_3, event_4));
        when(subscription_3.getEvents()).thenReturn(asList(event_5, event_6));
        when(subscription_4.getEvents()).thenReturn(asList(event_7, event_8));

        final List<SubscriptionsDescriptor> subscriptionsDescriptors = asList(subscriptionsDescriptor_1, subscriptionsDescriptor_2);

        when(subscriptionsDescriptorsRegistry.getAll()).thenReturn(subscriptionsDescriptors);

        final Map<String, String> eventNamesToSchemaIds = schemaIdMappingProvider.mapEventNamesToSchemaIds();

        assertThat(eventNamesToSchemaIds.size(), is(8));

        assertThat(eventNamesToSchemaIds.get("eventName_1"), is("schemaUrl_1"));
        assertThat(eventNamesToSchemaIds.get("eventName_2"), is("schemaUrl_2"));
        assertThat(eventNamesToSchemaIds.get("eventName_3"), is("schemaUrl_3"));
        assertThat(eventNamesToSchemaIds.get("eventName_4"), is("schemaUrl_4"));
        assertThat(eventNamesToSchemaIds.get("eventName_5"), is("schemaUrl_5"));
        assertThat(eventNamesToSchemaIds.get("eventName_6"), is("schemaUrl_6"));
        assertThat(eventNamesToSchemaIds.get("eventName_7"), is("schemaUrl_7"));
        assertThat(eventNamesToSchemaIds.get("eventName_8"), is("schemaUrl_8"));
    }

    @Test
    public void shouldHandleCopiesOfSchemaFiles() throws Exception {

        final Event event_1 = new Event("eventName_1", "schemaUrl_1");
        final Event event_2 = new Event("eventName_1", "schemaUrl_1");

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_2 = mock(SubscriptionsDescriptor.class);

        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);

        when(subscriptionsDescriptor_1.getSubscriptions()).thenReturn(asList(subscription_1, subscription_2));

        when(subscription_1.getEvents()).thenReturn(asList(event_1, event_2));

        final List<SubscriptionsDescriptor> subscriptionsDescriptors = asList(subscriptionsDescriptor_1, subscriptionsDescriptor_2);

        when(subscriptionsDescriptorsRegistry.getAll()).thenReturn(subscriptionsDescriptors);

        final Map<String, String> eventNamesToSchemaIds = schemaIdMappingProvider.mapEventNamesToSchemaIds();

        assertThat(eventNamesToSchemaIds.size(), is(1));

        assertThat(eventNamesToSchemaIds.get("eventName_1"), is("schemaUrl_1"));
    }
}
