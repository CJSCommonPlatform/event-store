package uk.gov.justice.services.eventstore.management.trigger;

import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventLogTriggerManipulator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

@Startup
@Singleton
public class TriggerManagementStartupBean {

    @Inject
    private EventLogTriggerManipulator eventLogTriggerManipulator;

    @PostConstruct
    public void addPublishingTriggerOnStartup() {
         eventLogTriggerManipulator.addTriggerToEventLogTable();
    }

    @PreDestroy
    public void dropPublishingTriggerOnShutdown() {
        eventLogTriggerManipulator.removeTriggerFromEventLogTable();
    }
}
