package uk.gov.justice.services.event.sourcing.subscription.error;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamError;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorRepository;
import uk.gov.justice.services.event.buffer.core.repository.subscription.StreamStatusJdbcRepository;

import java.util.UUID;

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
    private StreamStatusJdbcRepository streamStatusJdbcRepository;

    @InjectMocks
    private StreamErrorService streamErrorService;

    @Test
    public void shouldRemoveAllOldErrorsForStreamAndSaveNewError() throws Exception {

        final UUID streamErrorId = randomUUID();
        final UUID streamId = randomUUID();
        final Long positionInStream = 98239847L;
        final StreamError streamError = mock(StreamError.class);

        when(streamError.id()).thenReturn(streamErrorId);
        when(streamError.streamId()).thenReturn(streamId);
        when(streamError.positionInStream()).thenReturn(positionInStream);

        streamErrorService.markStreamAsErrored(streamError);

        final InOrder inOrder = inOrder(streamStatusJdbcRepository, streamErrorRepository);
        inOrder.verify(streamStatusJdbcRepository).unmarkStreamAsErrored(streamId);
        inOrder.verify(streamErrorRepository).removeAllErrorsForStream(streamId);
        inOrder.verify(streamErrorRepository).save(streamError);
        inOrder.verify(streamStatusJdbcRepository).markStreamAsErrored(streamId, streamErrorId, positionInStream);
    }
}