package uk.gov.justice.services.healthcheck.healthchecks;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.healthcheck.healthchecks.JobStoreHealthcheck.JOB_STORE_TABLE_NAMES;

import uk.gov.justice.framework.libraries.datasource.providers.jobstore.JndiJobStoreDataSourceProvider;
import uk.gov.justice.services.healthcheck.api.HealthcheckResult;
import uk.gov.justice.services.healthcheck.utils.database.TableChecker;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class JobStoreHealthcheckTest {

    @Mock
    private JndiJobStoreDataSourceProvider jndiJobStoreDataSourceProvider;

    @Mock
    private TableChecker tableChecker;

    @Mock
    private Logger logger;

    @InjectMocks
    private JobStoreHealthcheck jobStoreHealthcheck;

    @Test
    public void shouldReturnCorrectHealthcheckName() throws Exception {

        assertThat(jobStoreHealthcheck.getHealthcheckName(), is("job-store-healthcheck"));
    }

    @Test
    public void shouldReturnCorrectHealthcheckDescription() throws Exception {

        assertThat(jobStoreHealthcheck.healthcheckDescription(), is("Checks connectivity to the jobstore database and that all tables are available"));
    }

    @Test
    public void shouldGetListOfExpectedTablesFromEventStoreAsHealthcheck() throws Exception {

        final DataSource jobStoreDataSource = mock(DataSource.class);
        final HealthcheckResult healthcheckResult = mock(HealthcheckResult.class);

        when(jndiJobStoreDataSourceProvider.getJobStoreDataSource()).thenReturn(jobStoreDataSource);
        when(tableChecker.checkTables(JOB_STORE_TABLE_NAMES, jobStoreDataSource)).thenReturn(healthcheckResult);

        assertThat(jobStoreHealthcheck.runHealthcheck(), is(healthcheckResult));
    }

    @Test
    public void shouldReturnHealthcheckFailureIfAccessingTheEventStoreThrowsSqlException() throws Exception {

        final SQLException sqlException = new SQLException("Oops");
        final DataSource jobStoreDataSource = mock(DataSource.class);

        when(jndiJobStoreDataSourceProvider.getJobStoreDataSource()).thenReturn(jobStoreDataSource);
        when(tableChecker.checkTables(JOB_STORE_TABLE_NAMES, jobStoreDataSource)).thenThrow(sqlException);

        final HealthcheckResult healthcheckResult = jobStoreHealthcheck.runHealthcheck();

        assertThat(healthcheckResult.isPassed(), is(false));
        assertThat(healthcheckResult.getErrorMessage().isPresent(), is(true));
        assertThat(healthcheckResult.getErrorMessage(), is(of("Exception thrown accessing jobstore database. java.sql.SQLException: Oops")));

        verify(logger).error("Healthcheck for jobstore database failed.", sqlException);
    }
}