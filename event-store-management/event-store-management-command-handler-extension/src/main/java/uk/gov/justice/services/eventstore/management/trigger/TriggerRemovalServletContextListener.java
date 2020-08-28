package uk.gov.justice.services.eventstore.management.trigger;

import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventLogTriggerManipulator;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class TriggerRemovalServletContextListener implements ServletContextListener {

    @Inject
    private EventLogTriggerManipulator eventLogTriggerManipulator;

    @Override
    public void contextInitialized(final ServletContextEvent ignored) {
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        eventLogTriggerManipulator.removeTriggerFromEventLogTable();
    }
}
