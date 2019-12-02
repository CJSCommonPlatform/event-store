package uk.gov.justice.services.eventstore.management.trigger;

import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventLogTriggerManipulator;
import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;

public class TriggerLifecycleManager implements Extension {

    private final CdiInstanceResolver cdiInstanceResolver;

    public TriggerLifecycleManager() {
        this(new CdiInstanceResolver());
    }

    public TriggerLifecycleManager(final CdiInstanceResolver cdiInstanceResolver) {
        this.cdiInstanceResolver = cdiInstanceResolver;
    }

    public void afterDeploymentValidation(@SuppressWarnings("unused") @Observes final AfterDeploymentValidation event, final BeanManager beanManager) {

        final EventLogTriggerManipulator eventLogTriggerManipulator = cdiInstanceResolver.getInstanceOf(
                EventLogTriggerManipulator.class,
                beanManager);

        eventLogTriggerManipulator.addTriggerToEventLogTable();
    }
    
    public void beforeShutdown(@SuppressWarnings("unused") @Observes final BeforeShutdown event, final BeanManager beanManager) {

        final EventLogTriggerManipulator eventLogTriggerManipulator = cdiInstanceResolver.getInstanceOf(
                EventLogTriggerManipulator.class,
                beanManager);

        eventLogTriggerManipulator.removeTriggerFromEventLogTable();
    }
}
