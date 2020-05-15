package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class SchemaIdMappingProvider {

    @Inject
    private SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    public Map<String, String> mapEventNamesToSchemaIds() {

        final List<Event> events = subscriptionsDescriptorsRegistry.getAll().stream()
                .flatMap(subscriptionsDescriptor -> subscriptionsDescriptor.getSubscriptions().stream())
                .flatMap(subscription -> subscription.getEvents().stream())
                .collect(toList());


        final HashMap<String, String> mappedEvents = new HashMap<>();

        for (final Event event : events) {
            mappedEvents.put(event.getName(), event.getSchemaUri());
        }

        return mappedEvents;
    }
}
