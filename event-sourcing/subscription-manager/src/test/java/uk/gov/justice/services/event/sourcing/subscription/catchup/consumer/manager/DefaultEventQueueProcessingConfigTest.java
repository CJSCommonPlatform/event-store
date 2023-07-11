package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultEventQueueProcessingConfigTest {

    @InjectMocks
    private DefaultEventQueueProcessingConfig defaultEventQueueProcessingConfig;

    @Test
    public void shouldGetTheInjectedJndiValue() throws Exception {

        setField(defaultEventQueueProcessingConfig, "maxTotalEventsInProcess", "23");

        assertThat(defaultEventQueueProcessingConfig.getMaxTotalEventsInProcess(), is(23));
    }
}
