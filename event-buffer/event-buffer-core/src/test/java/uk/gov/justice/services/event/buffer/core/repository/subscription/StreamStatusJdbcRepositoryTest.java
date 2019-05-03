package uk.gov.justice.services.event.buffer.core.repository.subscription;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StreamStatusJdbcRepositoryTest {

    @SuppressWarnings("unused")
    @Spy
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory = new PreparedStatementWrapperFactory();

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Mock
    private PreparedStatement preparedStatement;

    @InjectMocks
    private StreamStatusJdbcRepository streamStatusJdbcRepository;

    @Before
    public void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
    }

    @Test
    public void shouldAttemptToInsert() throws Exception {
        //this is postgreSQL only, so we can't write repository level integration test,

        final String source = "a source";

        when(connection.prepareStatement("INSERT INTO stream_status (position, stream_id, source) VALUES (?, ?, ?) ON CONFLICT DO NOTHING"))
                .thenReturn(preparedStatement);

        final UUID streamId = randomUUID();
        final long position = 1l;
        streamStatusJdbcRepository.insertOrDoNothing(new Subscription(streamId, position, source));

        verify(preparedStatement).setLong(1, position);
        verify(preparedStatement).setObject(2, streamId);
        verify(preparedStatement).executeUpdate();
    }

}
