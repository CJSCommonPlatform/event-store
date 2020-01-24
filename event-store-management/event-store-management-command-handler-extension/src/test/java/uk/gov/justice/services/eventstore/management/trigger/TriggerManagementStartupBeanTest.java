package uk.gov.justice.services.eventstore.management.trigger;

import static org.mockito.Mockito.verify;

import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventLogTriggerManipulator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TriggerManagementStartupBeanTest {

    @Mock
    private EventLogTriggerManipulator eventLogTriggerManipulator;

    @InjectMocks
    private TriggerManagementStartupBean triggerManagementStartupBean;

    @Test
    public void shouldAddTriggerOnStartup() throws Exception {

        triggerManagementStartupBean.addPublishingTriggerOnStartup();

        verify(eventLogTriggerManipulator).addTriggerToEventLogTable();
    }

    @Test
    public void shouldRemoveTriggerOnShutdown() throws Exception {

        triggerManagementStartupBean.dropPublishingTriggerOnShutdown();

        verify(eventLogTriggerManipulator).removeTriggerFromEventLogTable();
    }
}
