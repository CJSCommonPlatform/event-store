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

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.sourcing.subscription.error.StreamProcessingException;
import uk.gov.justice.services.event.sourcing.subscription.error.StreamProcessingFailureHandler;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BackwardsCompatibleSubscriptionManagerTest {

    private final String componentName = "SOME_COMPONENT";
    private final InterceptorChainProcessor interceptorChainProcessor = mock(InterceptorChainProcessor.class);
    private final InterceptorContextProvider interceptorContextProvider = mock(InterceptorContextProvider.class);
    private final StreamProcessingFailureHandler streamProcessingFailureHandler = mock(StreamProcessingFailureHandler.class);

    @Captor
    private ArgumentCaptor<InterceptorContext> interceptorContextCaptor;

    private BackwardsCompatibleSubscriptionManager backwardsCompatibleSubscriptionManager = new BackwardsCompatibleSubscriptionManager(
            interceptorChainProcessor,
            interceptorContextProvider,
            streamProcessingFailureHandler,
            componentName
    );

    @SuppressWarnings("unchecked")
    @Test
    public void shouldProcessEnvelopeUsingInterceptorChain() throws Exception {

        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = mock(InterceptorContext.class);

        when(interceptorContextProvider.getInterceptorContext(incomingJsonEnvelope)).thenReturn(interceptorContext);

        backwardsCompatibleSubscriptionManager.process(incomingJsonEnvelope);

        final InOrder inOrder = inOrder(interceptorChainProcessor, streamProcessingFailureHandler);
        inOrder.verify(interceptorChainProcessor).process(interceptorContextCaptor.capture());
        inOrder.verify(streamProcessingFailureHandler).onStreamProcessingSucceeded(incomingJsonEnvelope, componentName);

        assertThat(interceptorContextCaptor.getValue(), is(interceptorContext));
    }

    @Test
    public void shouldCallStreamProcessingFailureOnException() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Goodness gracious me");
        final UUID eventId = fromString("adab819c-4e57-4d58-bcba-e832d35121be");
        final UUID streamId = fromString("0703f8b5-c003-47b2-ad20-f414e653e5f0");
        final String eventName = "context.events.some-event";

        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);
        final InterceptorContext interceptorContext = mock(InterceptorContext.class);

        when(incomingJsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.id()).thenReturn(eventId);
        when(metadata.name()).thenReturn(eventName);
        when(metadata.streamId()).thenReturn(of(streamId));
        when(interceptorContextProvider.getInterceptorContext(incomingJsonEnvelope)).thenReturn(interceptorContext);
        doThrow(nullPointerException).when(interceptorChainProcessor).process(interceptorContext);

        final StreamProcessingException streamProcessingException = assertThrows(
                StreamProcessingException.class,
                () -> backwardsCompatibleSubscriptionManager.process(incomingJsonEnvelope)
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
        when(interceptorContextProvider.getInterceptorContext(incomingJsonEnvelope)).thenReturn(interceptorContext);
        doThrow(nullPointerException).when(interceptorChainProcessor).process(interceptorContext);

        final StreamProcessingException streamProcessingException = assertThrows(
                StreamProcessingException.class,
                () -> backwardsCompatibleSubscriptionManager.process(incomingJsonEnvelope)
        );
        assertThat(streamProcessingException.getCause(), is(nullPointerException));
        assertThat(streamProcessingException.getMessage(), is("Failed to process event. name: 'context.events.some-event', eventId: 'adab819c-4e57-4d58-bcba-e832d35121be, streamId: 'null'"));

        verify(streamProcessingFailureHandler).onStreamProcessingFailure(incomingJsonEnvelope, nullPointerException, componentName);
    }
}
