package uk.gov.justice.services.subscription;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SpliteratorStreamFactoryTest {

    @InjectMocks
    private SpliteratorStreamFactory spliteratorStreamFactory;

    @Test
    public void shouldCreateStreamWithSpliterator() throws Exception {

        final ProcessedEventStreamSpliterator processedEventStreamSpliterator = mock(ProcessedEventStreamSpliterator.class);
        assertThat(spliteratorStreamFactory.createStreamFrom(processedEventStreamSpliterator), is(notNullValue()));
    }
}