package uk.gov.justice.services.event.buffer.core.repository.streamerror;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StreamErrorPersistenceTest {

    @Mock
    private StreamErrorHashPersistence streamErrorHashPersistence;

    @Mock
    private StreamErrorDetailsPersistence streamErrorDetailsPersistence;

    @InjectMocks
    private StreamErrorPersistence streamErrorPersistence;

    @Test
    public void shouldInsertStreamErrorAndUpsertStreamErrorHash() throws Exception {

        final StreamErrorDetails streamErrorDetails = mock(StreamErrorDetails.class);
        final StreamErrorHash streamErrorHash = mock(StreamErrorHash.class);
        final Connection connection = mock(Connection.class);

        when(streamErrorDetailsPersistence.insert(streamErrorDetails, connection)).thenReturn(1);

        final boolean atLeastOneEventProcessed = streamErrorPersistence.save(new StreamError(streamErrorDetails, streamErrorHash), connection);
        assertThat(atLeastOneEventProcessed, is(true));

        final InOrder inOrder = inOrder(streamErrorHashPersistence, streamErrorDetailsPersistence, connection);

        inOrder.verify(streamErrorHashPersistence).upsert(streamErrorHash, connection);
        inOrder.verify(streamErrorDetailsPersistence).insert(streamErrorDetails, connection);

        verify(connection, never()).close();
    }

    @Test
    public void shouldReturnFalseIfInsertIntoStreamErrorDoesNotUpdateAnyRows() throws Exception {

        final StreamErrorDetails streamErrorDetails = mock(StreamErrorDetails.class);
        final StreamErrorHash streamErrorHash = mock(StreamErrorHash.class);
        final Connection connection = mock(Connection.class);

        when(streamErrorDetailsPersistence.insert(streamErrorDetails, connection)).thenReturn(0);

        final boolean atLeastOneEventProcessed = streamErrorPersistence.save(new StreamError(streamErrorDetails, streamErrorHash), connection);
        assertThat(atLeastOneEventProcessed, is(false));

        final InOrder inOrder = inOrder(streamErrorHashPersistence, streamErrorDetailsPersistence, connection);

        inOrder.verify(streamErrorHashPersistence).upsert(streamErrorHash, connection);
        inOrder.verify(streamErrorDetailsPersistence).insert(streamErrorDetails, connection);

        verify(connection, never()).close();
    }

    @Test
    public void shouldThrowStreamErrorHandlingExceptionOnFailureToSave() throws Exception {

        final SQLException sqlException = new SQLException("Shiver me timbers");

        final StreamErrorDetails streamErrorDetails = mock(StreamErrorDetails.class);
        final StreamErrorHash streamErrorHash = mock(StreamErrorHash.class);
        final Connection connection = mock(Connection.class);

        when(streamErrorDetails.toString()).thenReturn(StreamErrorDetails.class.getSimpleName());
        when(streamErrorHash.toString()).thenReturn(StreamErrorHash.class.getSimpleName());

        doThrow(sqlException).when(streamErrorDetailsPersistence).insert(streamErrorDetails, connection);

        final StreamErrorHandlingException streamErrorHandlingException = assertThrows(
                StreamErrorHandlingException.class,
                () -> streamErrorPersistence.save(new StreamError(streamErrorDetails, streamErrorHash) , connection));

        assertThat(streamErrorHandlingException.getCause(), is(sqlException));
        assertThat(streamErrorHandlingException.getMessage(), is("Failed to save StreamError: StreamError[streamErrorDetails=StreamErrorDetails, streamErrorHash=StreamErrorHash]"));

        verify(connection, never()).close();
    }

    @Test
    public void shouldFindByStreamErrorId() throws Exception {

        final UUID streamErrorId = randomUUID();
        final String hash = "some-hash";

        final Connection connection = mock(Connection.class);
        final StreamErrorDetails streamErrorDetails = mock(StreamErrorDetails.class);
        final StreamErrorHash streamErrorHash = mock(StreamErrorHash.class);

        when(streamErrorDetailsPersistence.findBy(streamErrorId, connection)).thenReturn(of(streamErrorDetails));
        when(streamErrorDetails.hash()).thenReturn(hash);
        when(streamErrorHashPersistence.findByHash(streamErrorDetails.hash(), connection)).thenReturn(of(streamErrorHash));

        final Optional<StreamError> streamErrorOptional = streamErrorPersistence.findBy(streamErrorId, connection);

        assertThat(streamErrorOptional.isPresent(), is(true));
        assertThat(streamErrorOptional.get().streamErrorDetails(), is(streamErrorDetails));
        assertThat(streamErrorOptional.get().streamErrorHash(), is(streamErrorHash));

        verify(connection, never()).close();
    }

    @Test
    public void shouldReturnEmptyIfStreamErrorHashNotFound() throws Exception {

        final UUID streamErrorId = randomUUID();
        final String hash = "some-hash";

        final Connection connection = mock(Connection.class);
        final StreamErrorDetails streamErrorDetails = mock(StreamErrorDetails.class);

        when(streamErrorDetailsPersistence.findBy(streamErrorId, connection)).thenReturn(of(streamErrorDetails));
        when(streamErrorDetails.hash()).thenReturn(hash);
        when(streamErrorHashPersistence.findByHash(streamErrorDetails.hash(), connection)).thenReturn(empty());

        assertThat(streamErrorPersistence.findBy(streamErrorId, connection), is(empty()));

        verify(connection, never()).close();
    }

    @Test
    public void shouldReturnEmptyIfStreamErrorDetailsNotFound() throws Exception {

        final UUID streamErrorId = randomUUID();
        final Connection connection = mock(Connection.class);

        when(streamErrorDetailsPersistence.findBy(streamErrorId, connection)).thenReturn(empty());

        assertThat(streamErrorPersistence.findBy(streamErrorId, connection), is(empty()));

        verify(connection, never()).close();
    }

    @Test
    public void shouldThrowStreamErrorHandlingExceptionIfFindingByStreamErrorIdThrowsSqlexception() throws Exception {

        final SQLException sqlException = new SQLException();
        final UUID streamErrorId = fromString("f4ab7943-6220-45a0-8da9-200f5e877b67");
        final String hash = "some-hash";

        final Connection connection = mock(Connection.class);
        final StreamErrorDetails streamErrorDetails = mock(StreamErrorDetails.class);

        when(streamErrorDetailsPersistence.findBy(streamErrorId, connection)).thenReturn(of(streamErrorDetails));
        when(streamErrorDetails.hash()).thenReturn(hash);
        when(streamErrorHashPersistence.findByHash(streamErrorDetails.hash(), connection)).thenThrow(sqlException);

        final StreamErrorHandlingException streamErrorHandlingException = assertThrows(
                StreamErrorHandlingException.class,
                () -> streamErrorPersistence.findBy(streamErrorId, connection));

        assertThat(streamErrorHandlingException.getCause(), is(sqlException));
        assertThat(streamErrorHandlingException.getMessage(), is("Failed find StreamError by streamErrorId: 'f4ab7943-6220-45a0-8da9-200f5e877b67'"));

        verify(connection, never()).close();
    }

    @Test
    public void shouldRemoveErrorForStream() throws Exception {

        final UUID streamId = randomUUID();
        final String source = "some-source";
        final String componentName = "some-component";

        final Connection connection = mock(Connection.class);

        streamErrorPersistence.removeErrorForStream(streamId, source, componentName, connection);

        final InOrder inOrder = inOrder(streamErrorDetailsPersistence, streamErrorHashPersistence, connection);

        inOrder.verify(streamErrorDetailsPersistence).deleteBy(streamId, source, componentName, connection);
        inOrder.verify(streamErrorHashPersistence).deleteOrphanedHashes(connection);
        verify(connection, never()).close();
    }

    @Test
    public void shouldThrowStreamErrorHandlingExceptionIfRemovingErrorForStreamFails() throws Exception {

        final SQLException sqlException = new SQLException("Bunnies");
        final UUID streamId = fromString("ad6b76f1-96b7-423b-a2d0-4a922236c2ad");
        final String source = "some-source";
        final String componentName = "some-component";

        final Connection connection = mock(Connection.class);

        doThrow(sqlException).when(streamErrorHashPersistence).deleteOrphanedHashes(connection);

        final StreamErrorHandlingException streamErrorHandlingException = assertThrows(
                StreamErrorHandlingException.class,
                () -> streamErrorPersistence.removeErrorForStream(streamId, source, componentName, connection));

        assertThat(streamErrorHandlingException.getCause(), is(sqlException));
        assertThat(streamErrorHandlingException.getMessage(), is("Failed to remove error for stream. streamId: 'ad6b76f1-96b7-423b-a2d0-4a922236c2ad', source: 'some-source, component: 'some-component'"));

        verify(connection, never()).close();
    }
}