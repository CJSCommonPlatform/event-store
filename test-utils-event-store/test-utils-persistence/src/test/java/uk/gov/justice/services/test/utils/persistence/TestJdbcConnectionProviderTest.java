package uk.gov.justice.services.test.utils.persistence;


import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import uk.gov.justice.services.jdbc.persistence.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class TestJdbcConnectionProviderTest {

    @InjectMocks
    private TestJdbcConnectionProvider testJdbcConnectionProvider;

    @Test
    public void shouldGetConnectionToEventStore() throws Exception {
        try(final Connection connection = testJdbcConnectionProvider.getEventStoreConnection("framework")) {
            assertThat(connection, is(notNullValue()));
        }
    }

    @Test
    public void shouldGetConnectionToViewStore() throws Exception {
        try(final Connection connection = testJdbcConnectionProvider.getViewStoreConnection("framework")) {
            assertThat(connection, is(notNullValue()));
        }
    }

    @Test
    public void shouldGetConnectionToSystem() throws Exception {
        try(final Connection connection = testJdbcConnectionProvider.getSystemConnection("framework")) {
            assertThat(connection, is(notNullValue()));
        }
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void shouldThrowADataAccessExceptionIfTheConnectionToTheEventStoreFails() throws Exception {

        final String expectedErrorMessage =
                "Failed to get JDBC connection " +
                        "to my-non-existent-context Event Store. " +
                        "url: 'jdbc:postgresql://localhost/my-non-existent-contexteventstore', " +
                        "username 'my-non-existent-context', " +
                        "password 'my-non-existent-context'";

        try(final Connection connection = testJdbcConnectionProvider.getEventStoreConnection("my-non-existent-context")) {
            assertThat(connection, is(notNullValue()));
            fail();
        } catch(DataAccessException expected) {
            assertThat(expected.getCause(), is(instanceOf(SQLException.class)));
            assertThat(expected.getMessage(), is(expectedErrorMessage));
        }
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void shouldThrowADataAccessExceptionIfTheConnectionToTheViewStoreFails() throws Exception {

        final String expectedErrorMessage =
                "Failed to get JDBC connection " +
                        "to my-non-existent-context View Store. " +
                        "url: 'jdbc:postgresql://localhost/my-non-existent-contextviewstore', " +
                        "username 'my-non-existent-context', " +
                        "password 'my-non-existent-context'";

        try(final Connection connection = testJdbcConnectionProvider.getViewStoreConnection("my-non-existent-context")) {
            assertThat(connection, is(notNullValue()));
            fail();
        } catch(DataAccessException expected) {
            assertThat(expected.getCause(), is(instanceOf(SQLException.class)));
            assertThat(expected.getMessage(), is(expectedErrorMessage));
        }
    }
}
