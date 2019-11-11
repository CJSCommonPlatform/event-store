package uk.gov.justice.services.test.utils.eventlog;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.eventsourcing.util.sql.triggers.DatabaseTriggerManipulatorFactory;
import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventLogTriggerManipulator;
import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventStoreTriggerManipulatorProvider;
import uk.gov.justice.services.test.utils.persistence.SettableEventStoreDataSourceProvider;

import javax.sql.DataSource;

import org.slf4j.Logger;

public class EventLogTriggerManipulatorFactory {

    public EventLogTriggerManipulator create(final DataSource eventStoreDataSource) {

        final EventStoreTriggerManipulatorProvider eventStoreTriggerManipulatorProvider = eventStoreTriggerManipulatorProvider(eventStoreDataSource);

        final Logger logger = getLogger(EventLogTriggerManipulator.class);
        final EventLogTriggerManipulator eventLogTriggerManipulator = new EventLogTriggerManipulator();

        setField(eventLogTriggerManipulator, "eventStoreTriggerManipulatorProvider", eventStoreTriggerManipulatorProvider);
        setField(eventLogTriggerManipulator, "logger", logger);

        return eventLogTriggerManipulator;
    }

    private EventStoreTriggerManipulatorProvider eventStoreTriggerManipulatorProvider(final DataSource eventStoreDataSource) {
        final SettableEventStoreDataSourceProvider eventStoreDataSourceProvider = new SettableEventStoreDataSourceProvider();
        eventStoreDataSourceProvider.setDataSource(eventStoreDataSource);

        final DatabaseTriggerManipulatorFactory databaseTriggerManipulatorFactory = new DatabaseTriggerManipulatorFactory();

        final EventStoreTriggerManipulatorProvider eventStoreTriggerManipulatorProvider = new EventStoreTriggerManipulatorProvider();
        setField(eventStoreTriggerManipulatorProvider, "eventStoreDataSourceProvider", eventStoreDataSourceProvider);
        setField(eventStoreTriggerManipulatorProvider, "databaseTriggerManipulatorFactory", databaseTriggerManipulatorFactory);
        return eventStoreTriggerManipulatorProvider;
    }
}
