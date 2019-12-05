package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEventQueueProcessingConfigTest {

    @InjectMocks
    private DefaultEventQueueProcessingConfig defaultEventQueueProcessingConfig;

    @Test
    public void shouldGetTheInjectedJndiValue() throws Exception {

        setField(defaultEventQueueProcessingConfig, "maxTotalEventsInProcess", "23");

        assertThat(defaultEventQueueProcessingConfig.getMaxTotalEventsInProcess(), is(23));
    }
}
