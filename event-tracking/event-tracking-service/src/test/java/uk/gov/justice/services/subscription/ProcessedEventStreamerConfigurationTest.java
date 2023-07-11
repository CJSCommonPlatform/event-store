package uk.gov.justice.services.subscription;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProcessedEventStreamerConfigurationTest {

    @InjectMocks
    private ProcessedEventStreamerConfiguration processedEventStreamerConfiguration;

    @Test
    public void shouldGetTheProcessedEventBatchSizeJndiValue() throws Exception {

        final Long value = 10_000L;

        setField(processedEventStreamerConfiguration, "processedEventFetchBatchSize", "" + value);

        assertThat(processedEventStreamerConfiguration.getProcessedEventFetchBatchSize(), is(value));
    }
}