package uk.gov.justice.services.healthcheck.database;

import static java.lang.String.format;
import static uk.gov.justice.services.healthcheck.api.HealthcheckResult.failure;
import static uk.gov.justice.services.healthcheck.api.HealthcheckResult.success;

import uk.gov.justice.services.healthcheck.api.HealthcheckResult;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

public class TableChecker {

    @Inject
    private DatabaseTableLister databaseTableLister;

    public HealthcheckResult checkTables(final List<String> expectedTableNames, final DataSource dataSource) throws SQLException {

        final List<String> existingTables = databaseTableLister.listTables(dataSource);

        if(existingTables.containsAll(expectedTableNames)) {
            return success();
        }

        return failure(format("Tables missing from database. Expected '%s' found '%s'", expectedTableNames, existingTables));
    }
}
