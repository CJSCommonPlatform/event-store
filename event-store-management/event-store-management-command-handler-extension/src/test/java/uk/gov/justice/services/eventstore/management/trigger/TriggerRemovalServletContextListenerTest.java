package uk.gov.justice.services.eventstore.management.trigger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventLogTriggerManipulator;

import javax.servlet.ServletContextEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TriggerRemovalServletContextListenerTest {

    @Mock
    private EventLogTriggerManipulator eventLogTriggerManipulator;
    
    @InjectMocks
    private TriggerRemovalServletContextListener triggerRemovalServletContextListener;

    @Test
    public void shouldRemoveTriggerOnShutdown() throws Exception {

        final ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);
        triggerRemovalServletContextListener.contextDestroyed(servletContextEvent);

        verify(eventLogTriggerManipulator).removeTriggerFromEventLogTable();
    }
}