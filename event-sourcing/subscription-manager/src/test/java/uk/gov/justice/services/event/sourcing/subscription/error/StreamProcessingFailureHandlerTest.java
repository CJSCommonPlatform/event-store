package uk.gov.justice.services.event.sourcing.subscription.error;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.configuration.errors.event.EventErrorHandlingConfiguration;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamError;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StreamProcessingFailureHandlerTest {

    @Mock
    private ExceptionDetailsRetriever exceptionDetailsRetriever;

    @Mock
    private StreamErrorConverter streamErrorConverter;

    @Mock
    private StreamErrorService streamErrorService;

    @Mock
    private EventErrorHandlingConfiguration eventErrorHandlingConfiguration;

    @InjectMocks
    private StreamProcessingFailureHandler jsonEnvelopeProcessingFailureHandler;

    @Test
    public void shouldCreateEventErrorFromExceptionAndJsonEnvelopeAndSave() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException();
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final ExceptionDetails exceptionDetails = mock(ExceptionDetails.class);
        final StreamError streamError = mock(StreamError.class);

        when(eventErrorHandlingConfiguration.isEventErrorHandlingEnabled()).thenReturn(true);
        when(exceptionDetailsRetriever.getExceptionDetailsFrom(nullPointerException)).thenReturn(exceptionDetails);
        when(streamErrorConverter.asStreamError(exceptionDetails, jsonEnvelope)).thenReturn(streamError);

        jsonEnvelopeProcessingFailureHandler.onStreamProcessingFailure(jsonEnvelope, nullPointerException);

        verify(streamErrorService).markStreamAsErrored(streamError);
    }

    @Test
    public void shouldDoNothingIfErrorHandlingIsDisabled() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException();
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        when(eventErrorHandlingConfiguration.isEventErrorHandlingEnabled()).thenReturn(false);

        jsonEnvelopeProcessingFailureHandler.onStreamProcessingFailure(jsonEnvelope, nullPointerException);

        verifyNoInteractions(streamErrorService);
        verifyNoInteractions(exceptionDetailsRetriever);
        verifyNoInteractions(streamErrorConverter);
    }
}