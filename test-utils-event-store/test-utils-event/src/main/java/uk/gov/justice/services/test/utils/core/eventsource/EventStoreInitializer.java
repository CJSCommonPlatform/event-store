package uk.gov.justice.services.test.utils.core.eventsource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class EventStoreInitializer {

    public void initializeEventStore(final DataSource eventStoreDataSource) throws LiquibaseException, SQLException {
        try(final Connection connection = eventStoreDataSource.getConnection()) {
            final Liquibase liquibase = new Liquibase(
                    "liquibase-files/event-store-db-changelog.xml",
                    new ClassLoaderResourceAccessor(),
                    new JdbcConnection(connection));

            liquibase.dropAll();
            liquibase.update("");
        }
    }
}
