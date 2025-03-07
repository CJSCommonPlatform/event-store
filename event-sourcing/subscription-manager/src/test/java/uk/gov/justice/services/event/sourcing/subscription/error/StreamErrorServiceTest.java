package uk.gov.justice.services.event.sourcing.subscription.error;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamError;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorDetails;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorHandlingException;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorRepository;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamStatusErrorPersistence;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
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
public class StreamErrorServiceTest {

    @Mock
    private StreamErrorRepository streamErrorRepository;

    @Mock
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @Mock
    private StreamStatusErrorPersistence streamStatusErrorPersistence;

    @InjectMocks
    private StreamErrorService streamErrorService;

    @Test
    public void shouldSaveStreamErrorAndHash() throws Exception {

        final UUID streamErrorId = randomUUID();
        final UUID streamId = randomUUID();
        final Long positionInStream = 98239847L;
        final String componentName = "SOME_COMPONENT";
        final String source = "some-source";
        final StreamError streamError = mock(StreamError.class);
        final StreamErrorDetails streamErrorDetails = mock(StreamErrorDetails.class);

        final DataSource viewStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);
        when(viewStoreDataSource.getConnection()).thenReturn(connection);

        when(streamError.streamErrorDetails()).thenReturn(streamErrorDetails);
        when(streamErrorDetails.id()).thenReturn(streamErrorId);
        when(streamErrorDetails.streamId()).thenReturn(streamId);
        when(streamErrorDetails.positionInStream()).thenReturn(positionInStream);
        when(streamErrorDetails.componentName()).thenReturn(componentName);
        when(streamErrorDetails.source()).thenReturn(source);

        streamErrorService.markStreamAsErrored(streamError);

        final InOrder inOrder = inOrder(streamStatusErrorPersistence, streamErrorRepository);
        inOrder.verify(streamErrorRepository).save(streamError, connection);
        inOrder.verify(streamStatusErrorPersistence).markStreamAsErrored(
                streamId,
                streamErrorId,
                positionInStream,
                componentName,
                source,
                connection);
    }

    @Test
    public void shouldThrowStreamErrorHandlingExceptionIfGettingConnectionFailsWhenSavingStreamErrorAndHash() throws Exception {

        final StreamError streamError = mock(StreamError.class);
        final SQLException sqlException = new SQLException("Oops");

        final DataSource viewStoreDataSource = mock(DataSource.class);

        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);
        when(viewStoreDataSource.getConnection()).thenThrow(sqlException);


        final StreamErrorHandlingException streamErrorHandlingException = assertThrows(
                StreamErrorHandlingException.class,
                () -> streamErrorService.markStreamAsErrored(streamError));

        assertThat(streamErrorHandlingException.getCause(), is(sqlException));
        assertThat(streamErrorHandlingException.getMessage(), is("Failed to get connection to view-store"));
    }

    @Test
    public void shouldMarkStreamAsFixed() throws Exception {

        final UUID streamId = randomUUID();
        final String componentName = "SOME_COMPONENT";
        final String source = "some-source";

        final DataSource viewStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);
        when(viewStoreDataSource.getConnection()).thenReturn(connection);

        streamErrorService.markStreamAsFixed(streamId, source, componentName);

        final InOrder inOrder = inOrder(streamStatusErrorPersistence, streamErrorRepository);
        inOrder.verify(streamStatusErrorPersistence).unmarkStreamStatusAsErrored(streamId, source, componentName, connection);
        inOrder.verify(streamErrorRepository).removeErrorForStream(streamId, source, componentName, connection);
    }

    @Test
    public void shouldThrowStreamErrorHandlingExceptionIfGettingConnectionFailsWhenMarkingStreamAsFixed() throws Exception {

        final UUID streamId = randomUUID();
        final String componentName = "SOME_COMPONENT";
        final String source = "some-source";
        final SQLException sqlException = new SQLException("Ooops");

        final DataSource viewStoreDataSource = mock(DataSource.class);

        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);
        when(viewStoreDataSource.getConnection()).thenThrow(sqlException);

        final StreamErrorHandlingException streamErrorHandlingException = assertThrows(
                StreamErrorHandlingException.class,
                () -> streamErrorService.markStreamAsFixed(streamId, source, componentName));

        assertThat(streamErrorHandlingException.getCause(), is(sqlException));
        assertThat(streamErrorHandlingException.getMessage(), is("Failed to get connection to view-store"));

    }
}