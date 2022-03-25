package uk.gov.justice.services.healthcheck.database;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.healthcheck.api.HealthcheckResult;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TableCheckerTest {

    @Mock
    private DatabaseTableLister databaseTableLister;

    @InjectMocks
    private TableChecker tableChecker;

    @Test
    public void shouldPassHealthcheckIfAllExpectedTablesAreFound() throws Exception {

        final List<String> expectedTableNames = List.of(
                "table_1",
                "table_2",
                "table_3"
        );

        final List<String> actualTableNames = List.of(
                "table_1",
                "table_2",
                "table_3"
        );

        final DataSource dataSource = mock(DataSource.class);

        when(databaseTableLister.listTables(dataSource)).thenReturn(actualTableNames);

        final HealthcheckResult healthcheckResult = tableChecker.checkTables(expectedTableNames, dataSource);

        assertThat(healthcheckResult.isPassed(), is(true));
        assertThat(healthcheckResult.getErrorMessage(), is(empty()));
    }

    @Test
    public void shouldFailHealthcheckIfAllExpectedTablesAreFound() throws Exception {

        final List<String> expectedTableNames = List.of(
                "table_1",
                "table_2",
                "table_3"
        );

        final List<String> actualTableNames = List.of(
                "table_1",
                "table_3"
        );

        final DataSource dataSource = mock(DataSource.class);

        when(databaseTableLister.listTables(dataSource)).thenReturn(actualTableNames);

        final HealthcheckResult healthcheckResult = tableChecker.checkTables(expectedTableNames, dataSource);

        assertThat(healthcheckResult.isPassed(), is(false));
        assertThat(healthcheckResult.getErrorMessage(), is(of("Tables missing from database. Expected '[table_1, table_2, table_3]' found '[table_1, table_3]'")));
    }
}