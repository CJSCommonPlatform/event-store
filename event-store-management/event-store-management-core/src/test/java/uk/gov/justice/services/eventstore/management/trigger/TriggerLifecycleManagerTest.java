package uk.gov.justice.services.eventstore.management.trigger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventLogTriggerManipulator;
import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;

import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TriggerLifecycleManagerTest {

    @Mock
    private CdiInstanceResolver cdiInstanceResolver;

    @InjectMocks
    private TriggerLifecycleManager triggerLifecycleManager;

    @Test
    public void shouldAddTheTriggerOnApplicationDeployment() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);
        final EventLogTriggerManipulator eventLogTriggerManipulator = mock(EventLogTriggerManipulator.class);

        when(cdiInstanceResolver.getInstanceOf(EventLogTriggerManipulator.class, beanManager)).thenReturn(eventLogTriggerManipulator);

        triggerLifecycleManager.afterDeploymentValidation(event, beanManager);

        verify(eventLogTriggerManipulator).addTriggerToEventLogTable();
    }

    @Test
    public void shouldRemoveTheTriggerOnApplicationShutdown() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final BeforeShutdown event = mock(BeforeShutdown.class);
        final EventLogTriggerManipulator eventLogTriggerManipulator = mock(EventLogTriggerManipulator.class);

        when(cdiInstanceResolver.getInstanceOf(EventLogTriggerManipulator.class, beanManager)).thenReturn(eventLogTriggerManipulator);

        triggerLifecycleManager.beforeShutdown(event, beanManager);

        verify(eventLogTriggerManipulator).removeTriggerFromEventLogTable();
    }
}
