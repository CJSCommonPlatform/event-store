package uk.gov.justice.services.healthcheck.database;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.test.utils.core.jdbc.LiquibaseDatabaseBootstrapper;
import uk.gov.justice.services.test.utils.persistence.TestJdbcDataSourceProvider;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DatabaseTableListerIT {

    private static final String LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML = "liquibase-files/event-store-db-changelog.xml";

    private final TestJdbcDataSourceProvider testJdbcDataSourceProvider = new TestJdbcDataSourceProvider();

    private DatabaseTableLister databaseTableLister = new DatabaseTableLister();


    private final LiquibaseDatabaseBootstrapper liquibaseDatabaseBootstrapper = new LiquibaseDatabaseBootstrapper();


    @Before
    public void setupEventstoreDatabase() throws Exception {

        try (final Connection connection = testJdbcDataSourceProvider.getEventStoreDataSource("framework").getConnection()) {
            liquibaseDatabaseBootstrapper.bootstrap(
                    LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML,
                    connection);
        }
    }


    @Test
    public void shouldListAllTablesForGivenDatasourceExcludingLiquibaseTables() throws Exception {

        final DataSource eventStoreDataSource = testJdbcDataSourceProvider.getEventStoreDataSource("framework");
        final List<String> tableNames = databaseTableLister.listTables(eventStoreDataSource);

        assertThat(tableNames.size(), is(5));

        assertThat(tableNames, hasItem("event_stream"));
        assertThat(tableNames, hasItem("event_log"));
        assertThat(tableNames, hasItem("published_event"));
        assertThat(tableNames, hasItem("publish_queue"));
        assertThat(tableNames, hasItem("pre_publish_queue"));
    }
}