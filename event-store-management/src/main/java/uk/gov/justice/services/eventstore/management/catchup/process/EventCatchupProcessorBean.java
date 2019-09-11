package uk.gov.justice.services.eventstore.management.catchup.process;

import static javax.ejb.TransactionAttributeType.NEVER;
import static javax.ejb.TransactionManagementType.CONTAINER;
import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Stateless
@TransactionManagement(CONTAINER)
@TransactionAttribute(value = NEVER)
public class EventCatchupProcessorBean {

    @Inject
    EventCatchupProcessorFactory eventCatchupProcessorFactory;

    public void performEventCatchup(final CatchupContext catchupContext) {

        final EventCatchupProcessor eventCatchupProcessor = eventCatchupProcessorFactory.create();

        eventCatchupProcessor.performEventCatchup(catchupContext);
    }
}
