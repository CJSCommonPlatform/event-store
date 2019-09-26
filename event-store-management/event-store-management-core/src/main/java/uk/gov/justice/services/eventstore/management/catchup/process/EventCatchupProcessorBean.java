package uk.gov.justice.services.eventstore.management.catchup.process;

import static javax.ejb.TransactionAttributeType.NEVER;
import static javax.ejb.TransactionManagementType.CONTAINER;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.inject.Inject;

@Stateless
@TransactionManagement(CONTAINER)
@TransactionAttribute(value = NEVER)
public class EventCatchupProcessorBean {

    @Inject
    private EventCatchupProcessor eventCatchupProcessor;

    public void performEventCatchup(final CatchupSubscriptionContext catchupSubscriptionContext) {

        eventCatchupProcessor.performEventCatchup(catchupSubscriptionContext);
    }
}
