package uk.gov.justice.services.event.sourcing.subscription.manager.cdi;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.core.interceptor.DefaultContextPayload;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class InterceptorContextProviderTest {

    @InjectMocks
    private InterceptorContextProvider interceptorContextProvider;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldGetTheInterceptorContext() throws Exception {

        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = interceptorContextProvider.getInterceptorContext(incomingJsonEnvelope);

        final DefaultContextPayload defaultContextPayload = getValueOfField(interceptorContext, "input", DefaultContextPayload.class);
        final Optional<JsonEnvelope> envelope = getValueOfField(defaultContextPayload, "envelope", Optional.class);

        if(envelope.isPresent()) {
            assertThat(envelope.get(), is(incomingJsonEnvelope));
        } else {
            fail();
        }
    }
}
