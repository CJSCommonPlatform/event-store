package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.event.sourcing.subscription.manager.BackwardsCompatibleSubscriptionManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class BackwardsCompatibleSubscriptionManagerFactoryTest {

    @Mock
    InterceptorContextProvider interceptorContextProvider;

    @Mock
    InterceptorChainProcessorProducer interceptorChainProcessorProducer;

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
    }
}
