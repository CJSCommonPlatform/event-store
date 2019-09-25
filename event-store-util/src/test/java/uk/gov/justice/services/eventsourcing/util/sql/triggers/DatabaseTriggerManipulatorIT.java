package uk.gov.justice.services.eventsourcing.util.sql.triggers;

import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.test.utils.persistence.TestJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class DatabaseTriggerManipulatorIT {

    private static final String CONTEXT_NAME = "framework";
    private static final String TEMPORARY_TABLE_NAME = "for_testing_delete_me";

    private final TestJdbcDataSourceProvider testJdbcDataSourceProvider = new TestJdbcDataSourceProvider();
    private final DataSource eventStoreDataSource = testJdbcDataSourceProvider.getEventStoreDataSource(CONTEXT_NAME);

    private DatabaseTriggerManipulator databaseTriggerManipulator = new DatabaseTriggerManipulator(eventStoreDataSource);

    @Before
    public void createTemporaryTable()throws Exception {

        final String createTabelSql = "CREATE TABLE " + TEMPORARY_TABLE_NAME + " (name varchar(255))";

        try(final Connection connection = eventStoreDataSource.getConnection();
        final PreparedStatement preparedStatement = connection.prepareStatement(createTabelSql)) {
            preparedStatement.execute();
        }
    }

    @After
    public void dropTemporaryTable()throws Exception {

        final String dropTabelSql = "DROP TABLE " + TEMPORARY_TABLE_NAME;

        try(final Connection connection = eventStoreDataSource.getConnection();
        final PreparedStatement preparedStatement = connection.prepareStatement(dropTabelSql)) {
            preparedStatement.execute();
        }
    }

    @Test
    public void shouldAddRemoveAndListTriggersOnTable() throws Exception {

        final String triggerName = "temp_trigger";
        final String action = "EXECUTE PROCEDURE update_pre_publish_queue()";

        assertThat(databaseTriggerManipulator.listTriggersOnTable(TEMPORARY_TABLE_NAME).isEmpty(), is(true));

        databaseTriggerManipulator.addInsertTriggerToTable(triggerName, TEMPORARY_TABLE_NAME, action);

        final List<TriggerData> triggerData = databaseTriggerManipulator.listTriggersOnTable(TEMPORARY_TABLE_NAME);

        assertThat(triggerData.size(), is(1));
        assertThat(triggerData.get(0).getTriggerName(), is(triggerName));
        assertThat(triggerData.get(0).getTableName(), is(TEMPORARY_TABLE_NAME));
        assertThat(triggerData.get(0).getManipulationType(), is("INSERT"));
        assertThat(triggerData.get(0).getAction(), is(action));
        assertThat(triggerData.get(0).getTiming(), is("AFTER"));

        databaseTriggerManipulator.removeTriggerFromTable(triggerName, TEMPORARY_TABLE_NAME);

        assertThat(databaseTriggerManipulator.listTriggersOnTable(TEMPORARY_TABLE_NAME).isEmpty(), is(true));
    }

    @Test
    public void shouldFindTriggerByItsName() throws Exception {

        final String triggerName = "temp_trigger";
        final String action = "EXECUTE PROCEDURE update_pre_publish_queue()";

        assertThat(databaseTriggerManipulator.listTriggersOnTable(TEMPORARY_TABLE_NAME).isEmpty(), is(true));

        databaseTriggerManipulator.addInsertTriggerToTable(triggerName, TEMPORARY_TABLE_NAME, action);

        final Optional<TriggerData> triggerOnTable = databaseTriggerManipulator.findTriggerOnTable(triggerName, TEMPORARY_TABLE_NAME);

        if(triggerOnTable.isPresent()) {

            final TriggerData triggerData = triggerOnTable.get();

            assertThat(triggerData.getTriggerName(), is(triggerName));
            assertThat(triggerData.getTableName(), is(TEMPORARY_TABLE_NAME));
            assertThat(triggerData.getManipulationType(), is("INSERT"));
            assertThat(triggerData.getAction(), is(action));
            assertThat(triggerData.getTiming(), is("AFTER"));
        }
    }

    @Test
    public void shouldReturnEmptyIfNoTriggerExists() throws Exception {

        assertThat(databaseTriggerManipulator.findTriggerOnTable("some_other_trigger", TEMPORARY_TABLE_NAME), is(empty()));
    }
}
