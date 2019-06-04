package uk.gov.justice.services.eventstore.management.catchup.process;

import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Stateless
public class EventCatchupProcessorBean {

    @Inject
    EventCatchupProcessorFactory eventCatchupProcessorFactory;

    @Transactional(NOT_SUPPORTED)
    public void performEventCatchup(final Subscription subscription, final String componentName) {

        final EventCatchupProcessor eventCatchupProcessor = eventCatchupProcessorFactory.create();

        eventCatchupProcessor.performEventCatchup(subscription, componentName);
    }
}
