package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MaxRetryProviderTest {

    @InjectMocks
    private MaxRetryProvider maxRetryProvider;

    @Test
    public void shouldGetTheMaxRetriesInjectedByJNDI() throws Exception {

        final long maxRetry = 298374L;

        setField(maxRetryProvider, "maxRetry", maxRetry);

        assertThat(maxRetryProvider.getMaxRetry(), is(maxRetry));
    }
}
