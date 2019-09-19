package uk.gov.justice.services.eventsourcing.util.sql.triggers;

import javax.sql.DataSource;

public class DatabaseTriggerManipulatorFactory {

    public DatabaseTriggerManipulator databaseTriggerManipulator(final DataSource dataSource) {
        return new DatabaseTriggerManipulator(dataSource);
    }
}
