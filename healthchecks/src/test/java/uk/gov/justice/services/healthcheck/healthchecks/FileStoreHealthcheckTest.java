package uk.gov.justice.services.healthcheck.healthchecks;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.healthcheck.healthchecks.FileStoreHealthcheck.FILE_STORE_TABLE_NAMES;

import uk.gov.justice.services.fileservice.api.FileServiceException;
import uk.gov.justice.services.fileservice.repository.DataSourceProvider;
import uk.gov.justice.services.healthcheck.api.HealthcheckResult;
import uk.gov.justice.services.healthcheck.utils.database.TableChecker;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class FileStoreHealthcheckTest {

    @Mock
    private DataSourceProvider fileStoreDataSourceProvider;

    @Mock
    private TableChecker tableChecker;

    @Mock
    private Logger logger;

    @InjectMocks
    private FileStoreHealthcheck fileStoreHealthcheck;

    @Test
    public void shouldReturnCorrectHealthcheckName() throws Exception {
        assertThat(fileStoreHealthcheck.getHealthcheckName(), is("file-store-healthcheck"));
    }

    @Test
    public void shouldReturnCorrectHealthcheckDescription() throws Exception {
        assertThat(fileStoreHealthcheck.healthcheckDescription(), is("Checks connectivity to the filestore database and that all tables are available"));
    }

    @Test
    public void shouldHaveTheCorrectListOfTableNames() throws Exception {
        assertThat(FILE_STORE_TABLE_NAMES.size(), is(2));
        assertThat(FILE_STORE_TABLE_NAMES, hasItems("content", "metadata"));
    }

    @Test
    public void shouldGetListOfExpectedTablesFromFileStoreAsHealthcheck() throws Exception {

        final DataSource fileStoreDataSource = mock(DataSource.class);
        final HealthcheckResult healthcheckResult = mock(HealthcheckResult.class);

        when(fileStoreDataSourceProvider.getDatasource()).thenReturn(fileStoreDataSource);
        when(tableChecker.checkTables(FILE_STORE_TABLE_NAMES, fileStoreDataSource)).thenReturn(healthcheckResult);

        assertThat(fileStoreHealthcheck.runHealthcheck(), is(healthcheckResult));
    }

    @Test
    public void shouldReturnHealthcheckFailureIfAccessingTheFileStoreThrowsException() throws Exception {

        final FileServiceException fileServiceException = new FileServiceException("Oops");

        when(fileStoreDataSourceProvider.getDatasource()).thenThrow(fileServiceException);

        final HealthcheckResult healthcheckResult = fileStoreHealthcheck.runHealthcheck();

        assertThat(healthcheckResult.isPassed(), is(false));
        assertThat(healthcheckResult.getErrorMessage().isPresent(), is(true));
        assertThat(healthcheckResult.getErrorMessage(), is(of("Exception thrown accessing filestore database. uk.gov.justice.services.fileservice.api.FileServiceException: Oops")));

        verify(logger).error("Healthcheck for filestore database failed.", fileServiceException);
    }
}