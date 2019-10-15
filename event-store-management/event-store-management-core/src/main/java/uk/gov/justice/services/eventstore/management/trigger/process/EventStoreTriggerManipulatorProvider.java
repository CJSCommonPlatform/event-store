package uk.gov.justice.services.eventstore.management.trigger.process;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.eventsourcing.util.sql.triggers.DatabaseTriggerManipulator;
import uk.gov.justice.services.eventsourcing.util.sql.triggers.DatabaseTriggerManipulatorFactory;

import javax.inject.Inject;
import javax.sql.DataSource;

public class EventStoreTriggerManipulatorProvider {

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Inject
    public DatabaseTriggerManipulatorFactory databaseTriggerManipulatorFactory;

    public DatabaseTriggerManipulator getDatabaseTriggerManipulator() {
        final DataSource defaultDataSource = eventStoreDataSourceProvider.getDefaultDataSource();
        return databaseTriggerManipulatorFactory.databaseTriggerManipulator(defaultDataSource);
    }
}
