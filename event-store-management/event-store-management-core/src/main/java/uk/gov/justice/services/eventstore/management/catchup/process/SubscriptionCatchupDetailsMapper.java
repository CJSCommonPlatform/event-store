package uk.gov.justice.services.eventstore.management.catchup.process;

import uk.gov.justice.services.eventstore.management.events.catchup.SubscriptionCatchupDetails;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.stream.Stream;

public class SubscriptionCatchupDetailsMapper {

    public Stream<SubscriptionCatchupDetails> toSubscriptionCatchupDetails(final SubscriptionsDescriptor subscriptionsDescriptor) {

        final String componentName = subscriptionsDescriptor.getServiceComponent();

        return subscriptionsDescriptor.getSubscriptions()
                .stream()
                .map(subscription ->
                        new SubscriptionCatchupDetails(
                                subscription.getName(),
                                subscription.getEventSourceName(),
                                componentName
                        )
                );
    }
}
