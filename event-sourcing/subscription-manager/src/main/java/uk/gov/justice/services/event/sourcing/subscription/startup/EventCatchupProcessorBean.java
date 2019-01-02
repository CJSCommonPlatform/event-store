package uk.gov.justice.services.event.sourcing.subscription.startup;

import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import uk.gov.justice.services.event.sourcing.subscription.manager.EventCatchupProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories.EventCatchupProcessorFactory;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Stateless
public class EventCatchupProcessorBean {

    @Inject
    EventCatchupProcessorFactory eventCatchupProcessorFactory;

    @Transactional(NOT_SUPPORTED)
    public void performEventCatchup(final String componentName, final Subscription subscription) {

        final EventCatchupProcessor eventCatchupProcessor = eventCatchupProcessorFactory.createFor(componentName);

        eventCatchupProcessor.performEventCatchup(subscription);
    }
}