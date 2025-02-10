package uk.gov.justice.services.event.sourcing.subscription.error;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamError;
import uk.gov.justice.services.eventsourcing.source.api.streams.MissingStreamIdException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StreamErrorConverterTest {

    @Mock
    private ExceptionHashGenerator exceptionHashGenerator;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private StreamErrorConverter streamErrorConverter;

    @Test
    public void shouldConvertExceptionDetailsAndEventIntoEventError() throws Exception {

        final NullPointerException causeException = new NullPointerException("Ooops");
        final RuntimeException exception = new RuntimeException("Something went wrogn", causeException);

        final String fullStackTrace = "Full stack trace";
        final String javaClassName = "uk.gov.justice.eventbuffer.core.error.SomeFailingJavaClass";
        final String methodName = "someJavaMethod";
        final int lineNumber = 234;
        final Long positionInStream = 12345L;

        final String hash = "kshdkfhkjsdfhkjsdhfkj";
        final ZonedDateTime dateCreated = new UtcClock().now();
        final String eventName = "context.events.something.happened";
        final UUID eventId = randomUUID();
        final UUID streamId = randomUUID();

        final StackTraceElement earliestStackTraceElement = mock(StackTraceElement.class);
        final StackTraceElement laterStackTraceElement = mock(StackTraceElement.class);
        final JsonEnvelope event = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);

        final List<StackTraceElement> stackTraceElements = List.of(earliestStackTraceElement, laterStackTraceElement);
        final ExceptionDetails exceptionDetails = new ExceptionDetails(
                exception,
                of(causeException),
                stackTraceElements,
                fullStackTrace
        );

        when(clock.now()).thenReturn(dateCreated);
        when(exceptionHashGenerator.createHashStringFrom(
                earliestStackTraceElement,
                exception.getClass().getName(),
                of(causeException.getClass().getName()))).thenReturn(hash);
        when(event.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(eventName);
        when(metadata.id()).thenReturn(eventId);
        when(metadata.streamId()).thenReturn(of(streamId));
        when(metadata.position()).thenReturn(of(positionInStream));

        when(earliestStackTraceElement.getClassName()).thenReturn(javaClassName);
        when(earliestStackTraceElement.getMethodName()).thenReturn(methodName);
        when(earliestStackTraceElement.getLineNumber()).thenReturn(lineNumber);

        final StreamError streamError = streamErrorConverter.asStreamError(exceptionDetails, event);

        assertThat(streamError.id(), is(instanceOf(UUID.class)));
        assertThat(streamError.eventName(), is(eventName));
        assertThat(streamError.eventId(), is(eventId));
        assertThat(streamError.streamId(), is(streamId));
        assertThat(streamError.positionInStream(), is(positionInStream));
        assertThat(streamError.dateCreated(), is(dateCreated));
        assertThat(streamError.exceptionClassName(), is(exception.getClass().getName()));
        assertThat(streamError.exceptionMessage(), is("Something went wrogn"));
        assertThat(streamError.causeClassName(), is(of(causeException.getClass().getName())));
        assertThat(streamError.causeMessage(), is(of("Ooops")));
        assertThat(streamError.javaClassname(), is(javaClassName));
        assertThat(streamError.javaMethod(), is(methodName));
        assertThat(streamError.javaLineNumber(), is(lineNumber));
        assertThat(streamError.fullStackTrace(), is(fullStackTrace));
    }

    @Test
    public void shouldHandleMissingCauseException() throws Exception {

        final RuntimeException exception = new RuntimeException("Something went wrogn");

        final String fullStackTrace = "Full stack trace";
        final String javaClassName = "uk.gov.justice.eventbuffer.core.error.SomeFailingJavaClass";
        final String methodName = "someJavaMethod";
        final int lineNumber = 234;

        final String hash = "kshdkfhkjsdfhkjsdhfkj";
        final ZonedDateTime dateCreated = new UtcClock().now();
        final String eventName = "context.events.something.happened";
        final UUID eventId = randomUUID();
        final UUID streamId = randomUUID();
        final Long positionInStream = 987L;

        final StackTraceElement earliestStackTraceElement = mock(StackTraceElement.class);
        final StackTraceElement laterStackTraceElement = mock(StackTraceElement.class);
        final JsonEnvelope event = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);

        final List<StackTraceElement> stackTraceElements = List.of(earliestStackTraceElement, laterStackTraceElement);
        final ExceptionDetails exceptionDetails = new ExceptionDetails(
                exception,
                empty(),
                stackTraceElements,
                fullStackTrace
        );

        when(clock.now()).thenReturn(dateCreated);
        when(exceptionHashGenerator.createHashStringFrom(
                earliestStackTraceElement,
                exception.getClass().getName(),
                empty())).thenReturn(hash);
        when(event.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(eventName);
        when(metadata.id()).thenReturn(eventId);
        when(metadata.streamId()).thenReturn(of(streamId));
        when(metadata.position()).thenReturn(of(positionInStream));

        when(earliestStackTraceElement.getClassName()).thenReturn(javaClassName);
        when(earliestStackTraceElement.getMethodName()).thenReturn(methodName);
        when(earliestStackTraceElement.getLineNumber()).thenReturn(lineNumber);

        final StreamError streamError = streamErrorConverter.asStreamError(exceptionDetails, event);

        assertThat(streamError.id(), is(instanceOf(UUID.class)));
        assertThat(streamError.eventName(), is(eventName));
        assertThat(streamError.eventId(), is(eventId));
        assertThat(streamError.streamId(), is(streamId));
        assertThat(streamError.positionInStream(), is(positionInStream));
        assertThat(streamError.dateCreated(), is(dateCreated));
        assertThat(streamError.exceptionClassName(), is(exception.getClass().getName()));
        assertThat(streamError.exceptionMessage(), is("Something went wrogn"));
        assertThat(streamError.causeClassName(), is(empty()));
        assertThat(streamError.causeMessage(), is(empty()));
        assertThat(streamError.javaClassname(), is(javaClassName));
        assertThat(streamError.javaMethod(), is(methodName));
        assertThat(streamError.javaLineNumber(), is(lineNumber));
        assertThat(streamError.fullStackTrace(), is(fullStackTrace));
    }

    @Test
    public void shouldThrowMissingStreamIdExceptionIfStreamIdIsEmptyInJsonEnvelope() throws Exception {

        final NullPointerException causeException = new NullPointerException("Ooops");
        final RuntimeException exception = new RuntimeException("Something went wrogn", causeException);

        final String fullStackTrace = "Full stack trace";
        final String javaClassName = "uk.gov.justice.eventbuffer.core.error.SomeFailingJavaClass";
        final String methodName = "someJavaMethod";
        final int lineNumber = 234;

        final String hash = "kshdkfhkjsdfhkjsdhfkj";
        final String eventName = "context.events.something.happened";
        final UUID eventId = fromString("74a2b139-cba7-4430-b200-b322c3729b1f");

        final StackTraceElement earliestStackTraceElement = mock(StackTraceElement.class);
        final StackTraceElement laterStackTraceElement = mock(StackTraceElement.class);
        final JsonEnvelope event = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);


        final List<StackTraceElement> stackTraceElements = List.of(earliestStackTraceElement, laterStackTraceElement);
        final ExceptionDetails exceptionDetails = new ExceptionDetails(
                exception,
                of(causeException),
                stackTraceElements,
                fullStackTrace
        );

        when(exceptionHashGenerator.createHashStringFrom(
                earliestStackTraceElement,
                exception.getClass().getName(),
                of(causeException.getClass().getName()))).thenReturn(hash);
        when(event.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(eventName);
        when(metadata.id()).thenReturn(eventId);
        when(metadata.streamId()).thenReturn(empty());

        when(earliestStackTraceElement.getClassName()).thenReturn(javaClassName);
        when(earliestStackTraceElement.getMethodName()).thenReturn(methodName);
        when(earliestStackTraceElement.getLineNumber()).thenReturn(lineNumber);

        final MissingStreamIdException missingStreamIdException = assertThrows(
                MissingStreamIdException.class,
                () -> streamErrorConverter.asStreamError(exceptionDetails, event));

        assertThat(missingStreamIdException.getMessage(), is("No stream id found in event JsonEnvelope. Event name: 'context.events.something.happened', eventId: '74a2b139-cba7-4430-b200-b322c3729b1f'"));
    }

    @Test
    public void shouldThrowMissingPositionInStreamExceptionIfPositionIsEmptyInJsonEnvelope() throws Exception {

        final NullPointerException causeException = new NullPointerException("Ooops");
        final RuntimeException exception = new RuntimeException("Something went wrogn", causeException);

        final String fullStackTrace = "Full stack trace";
        final String javaClassName = "uk.gov.justice.eventbuffer.core.error.SomeFailingJavaClass";
        final String methodName = "someJavaMethod";
        final int lineNumber = 234;

        final String hash = "kshdkfhkjsdfhkjsdhfkj";
        final String eventName = "context.events.something.happened";
        final UUID eventId = fromString("7de6f031-8d1e-4e3b-9f03-112a9af80690");
        final UUID streamId = randomUUID();

        final StackTraceElement earliestStackTraceElement = mock(StackTraceElement.class);
        final StackTraceElement laterStackTraceElement = mock(StackTraceElement.class);
        final JsonEnvelope event = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);

        final List<StackTraceElement> stackTraceElements = List.of(earliestStackTraceElement, laterStackTraceElement);
        final ExceptionDetails exceptionDetails = new ExceptionDetails(
                exception,
                of(causeException),
                stackTraceElements,
                fullStackTrace
        );

        when(exceptionHashGenerator.createHashStringFrom(
                earliestStackTraceElement,
                exception.getClass().getName(),
                of(causeException.getClass().getName()))).thenReturn(hash);
        when(event.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(eventName);
        when(metadata.id()).thenReturn(eventId);
        when(metadata.streamId()).thenReturn(of(streamId));
        when(metadata.position()).thenReturn(empty());

        when(earliestStackTraceElement.getClassName()).thenReturn(javaClassName);
        when(earliestStackTraceElement.getMethodName()).thenReturn(methodName);
        when(earliestStackTraceElement.getLineNumber()).thenReturn(lineNumber);

        final MissingPoisitionInStreamException missingPoisitionInStreamException = assertThrows(
                MissingPoisitionInStreamException.class,
                () -> streamErrorConverter.asStreamError(exceptionDetails, event));

        assertThat(missingPoisitionInStreamException.getMessage(), is("No positionInStream found in event JsonEnvelope. Event name: 'context.events.something.happened', eventId: '7de6f031-8d1e-4e3b-9f03-112a9af80690'"));
    }
}