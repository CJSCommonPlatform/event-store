package uk.gov.justice.services.event.sourcing.subscription.manager;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.sourcing.subscription.error.StreamProcessingException;
import uk.gov.justice.services.event.sourcing.subscription.error.StreamProcessingFailureHandler;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

public class DefaultSubscriptionManagerTest {

    private final String componentName = "SOME_COMPONENT";
    private final EventBufferProcessor eventBufferProcessor = mock(EventBufferProcessor.class);
    private final StreamProcessingFailureHandler streamProcessingFailureHandler = mock(StreamProcessingFailureHandler.class);
    private DefaultSubscriptionManager defaultSubscriptionManager = new DefaultSubscriptionManager(
            eventBufferProcessor,
            streamProcessingFailureHandler,
            componentName
    );

    @Test
    public void shouldProcessWithEventBuffer() {

        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);

        defaultSubscriptionManager.process(incomingJsonEnvelope);

        final InOrder inOrder = inOrder(eventBufferProcessor, streamProcessingFailureHandler);
        inOrder.verify(eventBufferProcessor).processWithEventBuffer(incomingJsonEnvelope);
        inOrder.verify(streamProcessingFailureHandler).onStreamProcessingSucceeded(incomingJsonEnvelope, componentName);
    }

    @Test
    public void shouldCallStreamProcessingFailureOnException() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Goodness gracious me");
        final UUID eventId = fromString("adab819c-4e57-4d58-bcba-e832d35121be");
        final UUID streamId = fromString("0703f8b5-c003-47b2-ad20-f414e653e5f0");
        final String eventName = "context.events.some-event";

        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);

        when(incomingJsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.id()).thenReturn(eventId);
        when(metadata.name()).thenReturn(eventName);
        when(metadata.streamId()).thenReturn(of(streamId));
        doThrow(nullPointerException).when(eventBufferProcessor).processWithEventBuffer(incomingJsonEnvelope);

        final StreamProcessingException streamProcessingException = assertThrows(
                StreamProcessingException.class,
                () -> defaultSubscriptionManager.process(incomingJsonEnvelope)
        );
        assertThat(streamProcessingException.getCause(), is(nullPointerException));
        assertThat(streamProcessingException.getMessage(), is("Failed to process event. name: 'context.events.some-event', eventId: 'adab819c-4e57-4d58-bcba-e832d35121be, streamId: '0703f8b5-c003-47b2-ad20-f414e653e5f0'"));

        verify(streamProcessingFailureHandler).onStreamProcessingFailure(incomingJsonEnvelope, nullPointerException, componentName);
    }

    @Test
    public void shouldHandleMissingStreamIdOnStreamProcessingFailure() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Goodness gracious me");
        final UUID eventId = fromString("adab819c-4e57-4d58-bcba-e832d35121be");
        final Optional<UUID> emptyStreamId = empty();
        final String eventName = "context.events.some-event";

        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);
        final InterceptorContext interceptorContext = mock(InterceptorContext.class);

        when(incomingJsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.id()).thenReturn(eventId);
        when(metadata.name()).thenReturn(eventName);
        when(metadata.streamId()).thenReturn(emptyStreamId);
        doThrow(nullPointerException).when(eventBufferProcessor).processWithEventBuffer(incomingJsonEnvelope);

        final StreamProcessingException streamProcessingException = assertThrows(
                StreamProcessingException.class,
                () -> defaultSubscriptionManager.process(incomingJsonEnvelope)
        );
        assertThat(streamProcessingException.getCause(), is(nullPointerException));
        assertThat(streamProcessingException.getMessage(), is("Failed to process event. name: 'context.events.some-event', eventId: 'adab819c-4e57-4d58-bcba-e832d35121be, streamId: 'null'"));

        verify(streamProcessingFailureHandler).onStreamProcessingFailure(incomingJsonEnvelope, nullPointerException, componentName);
    }
}
