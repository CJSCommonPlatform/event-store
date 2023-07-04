package uk.gov.justice.services.healthcheck.healthchecks;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.healthcheck.healthchecks.SystemDatabaseHealthcheck.SYSTEM_DATABASE_TABLE_NAMES;

import uk.gov.justice.services.healthcheck.api.HealthcheckResult;
import uk.gov.justice.services.healthcheck.utils.database.TableChecker;
import uk.gov.justice.services.jdbc.persistence.SystemJdbcDataSourceProvider;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
@ExtendWith(MockitoExtension.class)
public class SystemDatabaseHealthcheckTest {

    @Mock
    private SystemJdbcDataSourceProvider systemJdbcDataSourceProvider;

    @Mock
    private TableChecker tableChecker;

    @Mock
    private Logger logger;

    @InjectMocks
    private SystemDatabaseHealthcheck systemDatabaseHealthcheck;

    @Test
    public void shouldReturnCorrectHealthcheckName() throws Exception {

        assertThat(systemDatabaseHealthcheck.getHealthcheckName(), is("system-database-healthcheck"));
    }

    @Test
    public void shouldReturnCorrectHealthcheckDescription() throws Exception {

        assertThat(systemDatabaseHealthcheck.healthcheckDescription(), is("Checks connectivity to the system database and that all tables are available"));
    }

    @Test
    public void shouldGetListOfExpectedTablesFromEventStoreAsHealthcheck() throws Exception {

        final DataSource systemDataSource = mock(DataSource.class);
        final HealthcheckResult healthcheckResult = mock(HealthcheckResult.class);

        when(systemJdbcDataSourceProvider.getDataSource()).thenReturn(systemDataSource);
        when(tableChecker.checkTables(SYSTEM_DATABASE_TABLE_NAMES, systemDataSource)).thenReturn(healthcheckResult);

        assertThat(systemDatabaseHealthcheck.runHealthcheck(), is(healthcheckResult));
    }

    @Test
    public void shouldReturnHealthcheckFailureIfAccessingTheEventStoreThrowsSqlException() throws Exception {

        final SQLException sqlException = new SQLException("Oops");
        final DataSource systemDataSource = mock(DataSource.class);

        when(systemJdbcDataSourceProvider.getDataSource()).thenReturn(systemDataSource);
        when(tableChecker.checkTables(SYSTEM_DATABASE_TABLE_NAMES, systemDataSource)).thenThrow(sqlException);

        final HealthcheckResult healthcheckResult = systemDatabaseHealthcheck.runHealthcheck();

        assertThat(healthcheckResult.isPassed(), is(false));
        assertThat(healthcheckResult.getErrorMessage().isPresent(), is(true));
        assertThat(healthcheckResult.getErrorMessage(), is(of("Exception thrown accessing system database. java.sql.SQLException: Oops")));

        verify(logger).error("Healthcheck for system database failed.", sqlException);
    }
}