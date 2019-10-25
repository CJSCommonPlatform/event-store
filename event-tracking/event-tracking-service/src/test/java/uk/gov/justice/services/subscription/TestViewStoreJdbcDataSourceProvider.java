package uk.gov.justice.services.subscription;

import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import javax.sql.DataSource;

public class TestViewStoreJdbcDataSourceProvider extends ViewStoreJdbcDataSourceProvider {

    private final DataSource viewStoreDataSource;

    public TestViewStoreJdbcDataSourceProvider(final DataSource viewStoreDataSource) {
        this.viewStoreDataSource = viewStoreDataSource;
    }

    @Override
    public synchronized DataSource getDataSource() {
        return viewStoreDataSource;
    }
}
