package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TableCleanerTest {

    @InjectMocks
    private DatabaseTableTruncator databaseTableTruncator;

    @Test
    public void shouldTruncateDatabaseTable() throws Exception {

        final String tableName = "published_event";
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("TRUNCATE published_event")).thenReturn(preparedStatement);

        databaseTableTruncator.truncate(tableName, dataSource);

        final InOrder inOrder = inOrder(preparedStatement, connection);

        inOrder.verify(preparedStatement).executeUpdate();
        inOrder.verify(connection).close();
    }
}
