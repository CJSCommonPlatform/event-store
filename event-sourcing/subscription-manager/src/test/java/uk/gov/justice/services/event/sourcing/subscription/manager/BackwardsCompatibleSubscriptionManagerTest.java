package uk.gov.justice.services.event.sourcing.subscription.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BackwardsCompatibleSubscriptionManagerTest {

    private final InterceptorChainProcessor interceptorChainProcessor = mock(InterceptorChainProcessor.class);
    private final InterceptorContextProvider interceptorContextProvider = mock(InterceptorContextProvider.class);

    @Captor
    private ArgumentCaptor<InterceptorContext> interceptorContextCaptor;

    private BackwardsCompatibleSubscriptionManager backwardsCompatibleSubscriptionManager = new BackwardsCompatibleSubscriptionManager(
            interceptorChainProcessor,
            interceptorContextProvider
    );

    @SuppressWarnings("unchecked")
    @Test
    public void shouldProcessEnvelopeUsingInterceptorChain() throws Exception {

        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = mock(InterceptorContext.class);

        when(interceptorContextProvider.getInterceptorContext(incomingJsonEnvelope)).thenReturn(interceptorContext);

        backwardsCompatibleSubscriptionManager.process(incomingJsonEnvelope);

        verify(interceptorChainProcessor).process(interceptorContextCaptor.capture());

        assertThat(interceptorContextCaptor.getValue(), is(interceptorContext));
    }
}
