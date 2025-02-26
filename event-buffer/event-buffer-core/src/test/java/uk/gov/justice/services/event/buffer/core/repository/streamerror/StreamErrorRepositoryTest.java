package uk.gov.justice.services.event.buffer.core.repository.streamerror;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StreamErrorRepositoryTest {

    @Mock
    private ViewStoreJdbcDataSourceProvider viewStoreDataSourceProvider;

    @Mock
    private StreamErrorHashPersistence streamErrorHashPersistence;

    @Mock
    private StreamErrorDetailsPersistence streamErrorDetailsPersistence;

    @InjectMocks
    private StreamErrorRepository streamErrorRepository;

    @Test
    public void shouldInsertStreamErrorAndUpsertStreamErrorHash() throws Exception {

        final StreamErrorDetails streamErrorDetails = mock(StreamErrorDetails.class);
        final StreamErrorHash streamErrorHash = mock(StreamErrorHash.class);

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(viewStoreDataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        streamErrorRepository.save(new StreamError(streamErrorDetails, streamErrorHash));

        final InOrder inOrder = inOrder(streamErrorHashPersistence, streamErrorDetailsPersistence, connection);

        inOrder.verify(streamErrorHashPersistence).upsert(streamErrorHash, connection);
        inOrder.verify(streamErrorDetailsPersistence).insert(streamErrorDetails, connection);
        inOrder.verify(connection).close();
    }

    @Test
    public void shouldThrowStreamErrorHandlingExceptionOnFailureToSave() throws Exception {

        final SQLException sqlException = new SQLException("Shiver me timbers");

        final StreamErrorDetails streamErrorDetails = mock(StreamErrorDetails.class);
        final StreamErrorHash streamErrorHash = mock(StreamErrorHash.class);

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(viewStoreDataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        when(streamErrorDetails.toString()).thenReturn(StreamErrorDetails.class.getSimpleName());
        when(streamErrorHash.toString()).thenReturn(StreamErrorHash.class.getSimpleName());

        doThrow(sqlException).when(streamErrorDetailsPersistence).insert(streamErrorDetails, connection);

        final StreamErrorHandlingException streamErrorHandlingException = assertThrows(
                StreamErrorHandlingException.class,
                () -> streamErrorRepository.save(new StreamError(streamErrorDetails, streamErrorHash)));

        assertThat(streamErrorHandlingException.getCause(), is(sqlException));
        assertThat(streamErrorHandlingException.getMessage(), is("Failed to save StreamError: StreamError[streamErrorDetails=StreamErrorDetails, streamErrorHash=StreamErrorHash]"));

        verify(connection).close();
    }

    @Test
    public void shouldFindByStreamErrorId() throws Exception {

        final UUID streamErrorId = randomUUID();
        final String hash = "some-hash";

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final StreamErrorDetails streamErrorDetails = mock(StreamErrorDetails.class);
        final StreamErrorHash streamErrorHash = mock(StreamErrorHash.class);

        when(viewStoreDataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(streamErrorDetailsPersistence.findBy(streamErrorId, connection)).thenReturn(of(streamErrorDetails));
        when(streamErrorDetails.hash()).thenReturn(hash);
        when(streamErrorHashPersistence.findByHash(streamErrorDetails.hash(), connection)).thenReturn(of(streamErrorHash));

        final Optional<StreamError> streamErrorOptional = streamErrorRepository.findBy(streamErrorId);

        assertThat(streamErrorOptional.isPresent(), is(true));
        assertThat(streamErrorOptional.get().streamErrorDetails(), is(streamErrorDetails));
        assertThat(streamErrorOptional.get().streamErrorHash(), is(streamErrorHash));

        verify(connection).close();
    }

    @Test
    public void shouldReturnEmptyIfStreamErrorHashNotFound() throws Exception {

        final UUID streamErrorId = randomUUID();
        final String hash = "some-hash";

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final StreamErrorDetails streamErrorDetails = mock(StreamErrorDetails.class);

        when(viewStoreDataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(streamErrorDetailsPersistence.findBy(streamErrorId, connection)).thenReturn(of(streamErrorDetails));
        when(streamErrorDetails.hash()).thenReturn(hash);
        when(streamErrorHashPersistence.findByHash(streamErrorDetails.hash(), connection)).thenReturn(empty());

        assertThat(streamErrorRepository.findBy(streamErrorId), is(empty()));

        verify(connection).close();
    }

    @Test
    public void shouldReturnEmptyIfStreamErrorDetailsNotFound() throws Exception {

        final UUID streamErrorId = randomUUID();

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(viewStoreDataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(streamErrorDetailsPersistence.findBy(streamErrorId, connection)).thenReturn(empty());

        assertThat(streamErrorRepository.findBy(streamErrorId), is(empty()));

        verify(connection).close();
    }

    @Test
    public void shouldThrowStreamErrorHandlingExceptionIfFindingByStreamErrorIdThrowsSqlexception() throws Exception {

        final SQLException sqlException = new SQLException();
        final UUID streamErrorId = fromString("f4ab7943-6220-45a0-8da9-200f5e877b67");
        final String hash = "some-hash";

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final StreamErrorDetails streamErrorDetails = mock(StreamErrorDetails.class);

        when(viewStoreDataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(streamErrorDetailsPersistence.findBy(streamErrorId, connection)).thenReturn(of(streamErrorDetails));
        when(streamErrorDetails.hash()).thenReturn(hash);
        when(streamErrorHashPersistence.findByHash(streamErrorDetails.hash(), connection)).thenThrow(sqlException);

        final StreamErrorHandlingException streamErrorHandlingException = assertThrows(
                StreamErrorHandlingException.class,
                () -> streamErrorRepository.findBy(streamErrorId));

        assertThat(streamErrorHandlingException.getCause(), is(sqlException));
        assertThat(streamErrorHandlingException.getMessage(), is("Failed find StreamError by streamErrorId: 'f4ab7943-6220-45a0-8da9-200f5e877b67'"));

        verify(connection).close();
    }

    @Test
    public void shouldRemoveErrorForStream() throws Exception {

        final UUID streamId = randomUUID();
        final String source = "some-source";
        final String componentName = "some-component";

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(viewStoreDataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        streamErrorRepository.removeErrorForStream(streamId, source, componentName);

        final InOrder inOrder = inOrder(streamErrorDetailsPersistence, streamErrorHashPersistence, connection);

        inOrder.verify(streamErrorDetailsPersistence).deleteBy(streamId, source, componentName, connection);
        inOrder.verify(streamErrorHashPersistence).deleteOrphanedHashes(connection);
        inOrder.verify(connection).close();
    }

    @Test
    public void shouldThrowStreamErrorHandlingExceptionIfRemovingErrorForStreamFails() throws Exception {

        final SQLException sqlException = new SQLException("Bunnies");
        final UUID streamId = fromString("ad6b76f1-96b7-423b-a2d0-4a922236c2ad");
        final String source = "some-source";
        final String componentName = "some-component";

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(viewStoreDataSourceProvider.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        doThrow(sqlException).when(streamErrorHashPersistence).deleteOrphanedHashes(connection);

        final StreamErrorHandlingException streamErrorHandlingException = assertThrows(
                StreamErrorHandlingException.class,
                () -> streamErrorRepository.removeErrorForStream(streamId, source, componentName));

        assertThat(streamErrorHandlingException.getCause(), is(sqlException));
        assertThat(streamErrorHandlingException.getMessage(), is("Failed to remove error for stream. streamId: 'ad6b76f1-96b7-423b-a2d0-4a922236c2ad', source: 'some-source, component: 'some-component'"));

        verify(connection).close();
    }
}