package uk.gov.justice.services.event.sourcing.subscription.error;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.configuration.errors.event.EventErrorHandlingConfiguration;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamError;
import uk.gov.justice.services.eventsourcing.source.api.streams.MissingStreamIdException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

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
    private StreamProcessingFailureHandler streamProcessingFailureHandler;

    @Test
    public void shouldCreateEventErrorFromExceptionAndJsonEnvelopeAndSave() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException();
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final ExceptionDetails exceptionDetails = mock(ExceptionDetails.class);
        final StreamError streamError = mock(StreamError.class);
        final String componentName = "SOME_COMPONENT";

        when(eventErrorHandlingConfiguration.isEventErrorHandlingEnabled()).thenReturn(true);
        when(exceptionDetailsRetriever.getExceptionDetailsFrom(nullPointerException)).thenReturn(exceptionDetails);
        when(streamErrorConverter.asStreamError(exceptionDetails, jsonEnvelope, componentName)).thenReturn(streamError);

        streamProcessingFailureHandler.onStreamProcessingFailure(jsonEnvelope, nullPointerException, componentName);

        verify(streamErrorService).markStreamAsErrored(streamError);
    }

    @Test
    public void shouldDoNothingIfErrorHandlingIsDisabled() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException();
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final String componentName = "SOME_COMPONENT";

        when(eventErrorHandlingConfiguration.isEventErrorHandlingEnabled()).thenReturn(false);

        streamProcessingFailureHandler.onStreamProcessingFailure(jsonEnvelope, nullPointerException, componentName);

        verifyNoInteractions(streamErrorService);
        verifyNoInteractions(exceptionDetailsRetriever);
        verifyNoInteractions(streamErrorConverter);
    }

    @Test
    public void shouldMarkStreamAsFixed() throws Exception {

        final UUID streamId = randomUUID();
        final String componentName = "SOME_COMPONENT";
        final String source = "some-source";

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);

        when(eventErrorHandlingConfiguration.isEventErrorHandlingEnabled()).thenReturn(true);
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.source()).thenReturn(of(source));
        when(metadata.streamId()).thenReturn(of(streamId));

        streamProcessingFailureHandler.onStreamProcessingSucceeded(jsonEnvelope, componentName);

        verify(streamErrorService).markStreamAsFixed(streamId, source, componentName);
    }

    @Test
    public void shouldDoNothingIfErrorHandlingIsDisabledWhenMarkingStreamAsFixed() throws Exception {

        final String componentName = "SOME_COMPONENT";
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        when(eventErrorHandlingConfiguration.isEventErrorHandlingEnabled()).thenReturn(false);

        streamProcessingFailureHandler.onStreamProcessingSucceeded(jsonEnvelope, componentName);

        verifyNoInteractions(streamErrorService);
    }

    @Test
    public void shouldThrowMissingSourceExceptionWhenMarkingStreamAsFixedIfNoSourceDefinedInJsonEnvelope() throws Exception {

        final UUID eventId = fromString("2bc75170-29e6-4203-9239-33b8ef6573e5");
        final String eventName = "some-context.events.something-happened";
        final String componentName = "SOME_COMPONENT";

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);

        when(eventErrorHandlingConfiguration.isEventErrorHandlingEnabled()).thenReturn(true);
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.source()).thenReturn(empty());
        when(metadata.id()).thenReturn(eventId);
        when(metadata.name()).thenReturn(eventName);

        final MissingSourceException missingSourceException = assertThrows(
                MissingSourceException.class,
                () -> streamProcessingFailureHandler.onStreamProcessingSucceeded(jsonEnvelope, componentName));

        assertThat(missingSourceException.getMessage(), is("No 'source' defined in event with id: '2bc75170-29e6-4203-9239-33b8ef6573e5' and eventName: 'some-context.events.something-happened'"));
    }

    @Test
    public void shouldThrowMissingStreamIdExceptionWhenMarkingStreamAsFixedIfNoStreamIdDefinedInJsonEnvelope() throws Exception {

        final UUID eventId = fromString("2bc75170-29e6-4203-9239-33b8ef6573e6");
        final String eventName = "some-context.events.something-happened";
        final String source = "source";
        final String componentName = "SOME_COMPONENT";

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);

        when(eventErrorHandlingConfiguration.isEventErrorHandlingEnabled()).thenReturn(true);
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.source()).thenReturn(of(source));
        when(metadata.streamId()).thenReturn(empty());
        when(metadata.id()).thenReturn(eventId);
        when(metadata.name()).thenReturn(eventName);

        final MissingStreamIdException missingStreamIdException = assertThrows(
                MissingStreamIdException.class,
                () -> streamProcessingFailureHandler.onStreamProcessingSucceeded(jsonEnvelope, componentName));

        assertThat(missingStreamIdException.getMessage(), is("No streamId defined in event with id: '2bc75170-29e6-4203-9239-33b8ef6573e6' and eventName: 'some-context.events.something-happened'"));
    }
}