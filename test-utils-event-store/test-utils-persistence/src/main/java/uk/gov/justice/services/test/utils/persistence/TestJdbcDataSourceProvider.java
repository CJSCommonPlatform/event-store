package uk.gov.justice.services.test.utils.persistence;

import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

public class TestJdbcDataSourceProvider {

    private static final int PORT = 5432;

    public DataSource getEventStoreDataSource(final String contextName) {

        final String databaseName = contextName + "eventstore";

        return getDataSource(contextName, databaseName);
    }

    public DataSource getViewStoreDataSource(final String contextName) {

        final String databaseName = contextName + "viewstore";

        return getDataSource(contextName, databaseName);
    }

    public DataSource getSystemDataSource(final String contextName) {

        final String databaseName = contextName + "system";

        return getDataSource(contextName, databaseName);
    }

    public DataSource getFileStoreDataSource(final String contextName) {

        final String databaseName = contextName + "filestore";

        return getDataSource(contextName, databaseName);
    }

    private DataSource getDataSource(final String contextName, final String databaseName) {

        final PGSimpleDataSource dataSource = new PGSimpleDataSource();

        dataSource.setServerName(getHost());
        dataSource.setPortNumber(PORT);
        dataSource.setDatabaseName(databaseName);
        dataSource.setUser(contextName);
        dataSource.setPassword(contextName);

        return dataSource;
    }
}
