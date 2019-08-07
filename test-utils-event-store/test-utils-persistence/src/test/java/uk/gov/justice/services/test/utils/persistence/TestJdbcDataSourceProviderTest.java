package uk.gov.justice.services.test.utils.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.Test;

public class TestJdbcDataSourceProviderTest {

    private final TestJdbcDataSourceProvider testJdbcDataSourceProvider = new TestJdbcDataSourceProvider();

    @Test
    public void shouldGetDataSourceToTheEventStoreDatabase() throws Exception {

        final String query = "SELECT current_database();";

        final DataSource eventStoreDataSource = testJdbcDataSourceProvider.getEventStoreDataSource("framework");

        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                assertThat(resultSet.getString(1), is("frameworkeventstore"));
            } else {
                fail();
            }
        }
    }

    @Test
    public void shouldGetDataSourceToTheViewStoreDatabase() throws Exception {

        final String query = "SELECT current_database();";

        final DataSource eventStoreDataSource = testJdbcDataSourceProvider.getViewStoreDataSource("framework");

        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                assertThat(resultSet.getString(1), is("frameworkviewstore"));
            } else {
                fail();
            }
        }
    }

    @Test
    public void shouldGetDataSourceToTheSystemDatabase() throws Exception {

        final String query = "SELECT current_database();";

        final DataSource eventStoreDataSource = testJdbcDataSourceProvider.getSystemDataSource("framework");

        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                assertThat(resultSet.getString(1), is("frameworksystem"));
            } else {
                fail();
            }
        }
    }

    @Test
    public void shouldGetDataSourceToTheFileStoreDatabase() throws Exception {

        final String query = "SELECT current_database();";

        final DataSource eventStoreDataSource = testJdbcDataSourceProvider.getFileStoreDataSource("framework");

        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                assertThat(resultSet.getString(1), is("frameworkfilestore"));
            } else {
                fail();
            }
        }
    }
}
