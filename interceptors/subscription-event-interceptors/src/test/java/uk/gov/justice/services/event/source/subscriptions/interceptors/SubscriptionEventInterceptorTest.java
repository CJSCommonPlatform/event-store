package uk.gov.justice.services.event.source.subscriptions.interceptors;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;

import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.source.subscriptions.repository.jdbc.SubscriptionsRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionEventInterceptorTest {

    @Mock
    private SubscriptionsRepository subscriptionsRepository;

    @Mock
    private Logger logger;

    @InjectMocks
    private SubscriptionEventInterceptor subscriptionEventInterceptor;

    @Test
    public void shouldUpdateCurrentEventNumberIfNewEventNumberGreaterThanCurrentEventNumber() {

        final InterceptorContext interceptorContext = mock(InterceptorContext.class);
        final InterceptorChain interceptorChain = mock(InterceptorChain.class);
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final String subscriptionName = "example";
        final long eventNumber = 10;
        final long currentEventNumber = 9;

        final Metadata metadata = metadataBuilder()
                .withId(randomUUID())
                .withName("example.event")
                .withSource(subscriptionName)
                .withEventNumber(eventNumber)
                .withPreviousEventNumber(currentEventNumber)
                .build();

        when(interceptorChain.processNext(interceptorContext)).thenReturn(interceptorContext);
        when(interceptorContext.inputEnvelope()).thenReturn(jsonEnvelope);
        when(jsonEnvelope.metadata()).thenReturn(metadata);

        final InterceptorContext resultInterceptorContext = subscriptionEventInterceptor.process(interceptorContext, interceptorChain);

        assertThat(resultInterceptorContext, is(interceptorContext));

        verify(subscriptionsRepository).insertOrUpdateCurrentEventNumber(eventNumber, subscriptionName);
    }

    @Test
    public void shouldLogWarningIfNoEventNumberInMetadata() {

        final InterceptorContext interceptorContext = mock(InterceptorContext.class);
        final InterceptorChain interceptorChain = mock(InterceptorChain.class);
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final String subscriptionName = "example";

        final Metadata metadata = metadataBuilder()
                .withId(randomUUID())
                .withName("example.event")
                .withSource(subscriptionName)
                .build();

        when(interceptorChain.processNext(interceptorContext)).thenReturn(interceptorContext);
        when(interceptorContext.inputEnvelope()).thenReturn(jsonEnvelope);
        when(jsonEnvelope.metadata()).thenReturn(metadata);

        final InterceptorContext resultInterceptorContext = subscriptionEventInterceptor.process(interceptorContext, interceptorChain);

        assertThat(resultInterceptorContext, is(interceptorContext));
        verify(logger).warn("Event with name example.event has no event number.");
    }

    @Test
    public void shouldLogWarningIfNoSourceInMetadata() {

        final InterceptorContext interceptorContext = mock(InterceptorContext.class);
        final InterceptorChain interceptorChain = mock(InterceptorChain.class);
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final long eventNumber = 9;

        final Metadata metadata = metadataBuilder()
                .withId(randomUUID())
                .withName("example.event")
                .withEventNumber(eventNumber)
                .withPreviousEventNumber(8L)
                .build();

        when(interceptorChain.processNext(interceptorContext)).thenReturn(interceptorContext);
        when(interceptorContext.inputEnvelope()).thenReturn(jsonEnvelope);
        when(jsonEnvelope.metadata()).thenReturn(metadata);

        final InterceptorContext resultInterceptorContext = subscriptionEventInterceptor.process(interceptorContext, interceptorChain);

        assertThat(resultInterceptorContext, is(interceptorContext));
        verify(logger).warn("Event with name example.event has no source.");
    }
}
