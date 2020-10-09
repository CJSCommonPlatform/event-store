package uk.gov.justice.services.test.utils.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.Test;

public class TestJdbcDataSourceProviderTest {

    private TestJdbcDataSourceProvider testJdbcDataSourceProvider = new TestJdbcDataSourceProvider();

    @Test
    public void shouldGetADataSourceToTheViewStore() throws Exception {

        final DataSource dataSource = testJdbcDataSourceProvider.getViewStoreDataSource("framework");

        try(final Connection connection = dataSource.getConnection()) {
            assertThat(connection.isClosed(), is(false));
        }
    }

    @Test
    public void shouldGetADataSourceToTheEventStore() throws Exception {

        final DataSource dataSource = testJdbcDataSourceProvider.getEventStoreDataSource("framework");

        try(final Connection connection = dataSource.getConnection()) {
            assertThat(connection.isClosed(), is(false));
        }
    }

    @Test
    public void shouldGetADataSourceToSystem() throws Exception {

        final DataSource dataSource = testJdbcDataSourceProvider.getSystemDataSource("framework");

        try(final Connection connection = dataSource.getConnection()) {
            assertThat(connection.isClosed(), is(false));
        }
    }

    @Test
    public void shouldGetADataSourceToFileStore() throws Exception {

        final DataSource dataSource = testJdbcDataSourceProvider.getFileStoreDataSource("framework");

        try(final Connection connection = dataSource.getConnection()) {
            assertThat(connection.isClosed(), is(false));
        }
    }

    @Test
    public void shouldGetDataSourceToFileService() throws Exception {

        final DataSource dataSource = testJdbcDataSourceProvider.getFileServiceDataSource();

        try(final Connection connection = dataSource.getConnection()) {
            assertThat(connection.isClosed(), is(false));
        }
    }
}
