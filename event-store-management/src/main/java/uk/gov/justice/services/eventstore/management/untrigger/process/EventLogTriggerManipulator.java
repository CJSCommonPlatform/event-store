package uk.gov.justice.services.eventstore.management.untrigger.process;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.util.sql.triggers.DatabaseTriggerManipulator;

import javax.inject.Inject;

import org.slf4j.Logger;

public class EventLogTriggerManipulator {

    private static final String TRIGGER_NAME = "queue_publish_event";
    private static final String TABLE_NAME = "event_log";
    private static final String ACTION = "EXECUTE PROCEDURE update_publish_queue()";

    @Inject
    private EventStoreTriggerManipulatorProvider eventStoreTriggerManipulatorProvider;

    @Inject
    private Logger logger;

    public void addTriggerToEventLogTable() {

        final DatabaseTriggerManipulator databaseTriggerManipulator = eventStoreTriggerManipulatorProvider
                .getDatabaseTriggerManipulator();

        if (databaseTriggerManipulator.findTriggerOnTable(TRIGGER_NAME, TABLE_NAME).isPresent()) {
            logger.warn(format("Trigger '%s' already exists on %s table", TRIGGER_NAME, TABLE_NAME));
        } else {

            databaseTriggerManipulator
                    .addInsertTriggerToTable(TRIGGER_NAME, TABLE_NAME, ACTION);

            logger.info(format("Trigger '%s' successfully added to %s table", TRIGGER_NAME, TABLE_NAME));
        }
    }

    public void removeTriggerFromEventLogTable() {

        final DatabaseTriggerManipulator databaseTriggerManipulator = eventStoreTriggerManipulatorProvider
                .getDatabaseTriggerManipulator();

        if (databaseTriggerManipulator.findTriggerOnTable(TRIGGER_NAME, TABLE_NAME).isPresent()) {

            databaseTriggerManipulator
                    .removeTriggerFromTable(TRIGGER_NAME, TABLE_NAME);

            logger.info(format("Removed trigger '%s' from %s table", TRIGGER_NAME, TABLE_NAME));
        } else {
            logger.warn(format("No trigger named '%s' found on %s table", TRIGGER_NAME, TABLE_NAME));
        }
    }
}
