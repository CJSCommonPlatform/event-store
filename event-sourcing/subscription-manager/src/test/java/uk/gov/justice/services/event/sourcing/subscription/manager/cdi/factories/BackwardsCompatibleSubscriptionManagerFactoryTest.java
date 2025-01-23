package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.event.sourcing.subscription.error.StreamProcessingFailureHandler;
import uk.gov.justice.services.event.sourcing.subscription.manager.BackwardsCompatibleSubscriptionManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class BackwardsCompatibleSubscriptionManagerFactoryTest {

    @Mock
    private InterceptorContextProvider interceptorContextProvider;

    @Mock
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Mock
    private StreamProcessingFailureHandler streamProcessingFailureHandler;

    @InjectMocks
    private BackwardsCompatibleSubscriptionManagerFactory backwardsCompatibleSubscriptionManagerFactory;

    @Test
    public void shouldCreateBackwardsCompatibleSubscriptionManager() throws Exception {

        final String componentName = "component name";

        final InterceptorChainProcessor interceptorChainProcessor = mock(InterceptorChainProcessor.class);

        when(interceptorChainProcessorProducer.produceLocalProcessor(componentName)).thenReturn(interceptorChainProcessor);

        final BackwardsCompatibleSubscriptionManager subscriptionManager = backwardsCompatibleSubscriptionManagerFactory
                .create(componentName);

        assertThat(getValueOfField(subscriptionManager, "interceptorChainProcessor", InterceptorChainProcessor.class), is(interceptorChainProcessor));
        assertThat(getValueOfField(subscriptionManager, "interceptorContextProvider", InterceptorContextProvider.class), is(interceptorContextProvider));
        assertThat(getValueOfField(subscriptionManager, "streamProcessingFailureHandler", StreamProcessingFailureHandler.class), is(streamProcessingFailureHandler));
    }
}
