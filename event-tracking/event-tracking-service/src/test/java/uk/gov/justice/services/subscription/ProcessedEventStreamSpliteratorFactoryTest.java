package uk.gov.justice.services.subscription;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ProcessedEventStreamSpliteratorFactoryTest {

    @Mock
    private ProcessedEventTrackingRepository processedEventTrackingRepository;
    
    @InjectMocks
    private ProcessedEventStreamSpliteratorFactory processedEventStreamSpliteratorFactory;

    @Test
    public void shouldCreateProcessedEventStreamSpliterator() throws Exception {

        final String source = "source";
        final String component = "component";
        final long batchSize = 23L;

        final ProcessedEventStreamSpliterator processedEventStreamSpliterator = processedEventStreamSpliteratorFactory.getProcessedEventStreamSpliterator(
                source,
                component,
                batchSize);

        assertThat(processedEventStreamSpliterator, is(notNullValue()));
        assertThat(getValueOfField(processedEventStreamSpliterator, "source", String.class), is(source));
        assertThat(getValueOfField(processedEventStreamSpliterator, "component", String.class), is(component));
        assertThat(getValueOfField(processedEventStreamSpliterator, "batchSize", Long.class), is(batchSize));
        assertThat(getValueOfField(processedEventStreamSpliterator, "processedEventTrackingRepository", ProcessedEventTrackingRepository.class), is(processedEventTrackingRepository));
    }
}