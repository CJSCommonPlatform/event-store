package uk.gov.justice.services.event.sourcing.subscription.manager.cdi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.test.utils.FieldAccessor.getFieldFrom;

import uk.gov.justice.services.core.interceptor.DefaultContextPayload;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class InterceptorContextProviderTest {

    @InjectMocks
    private InterceptorContextProvider interceptorContextProvider;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldGetTheInterceptorContext() throws Exception {

        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = interceptorContextProvider.getInterceptorContext(incomingJsonEnvelope);

        final DefaultContextPayload defaultContextPayload = getFieldFrom(interceptorContext, "input", DefaultContextPayload.class);
        final Optional<JsonEnvelope> envelope = getFieldFrom(defaultContextPayload, "envelope", Optional.class);

        if(envelope.isPresent()) {
            assertThat(envelope.get(), is(incomingJsonEnvelope));
        } else {
            fail();
        }
    }
}
