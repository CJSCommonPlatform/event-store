package uk.gov.justice.services.subscription;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpliteratorStreamFactoryTest {

    @InjectMocks
    private SpliteratorStreamFactory spliteratorStreamFactory;

    @Test
    public void shouldCreateStreamWithSpliterator() throws Exception {

        final ProcessedEventStreamSpliterator processedEventStreamSpliterator = mock(ProcessedEventStreamSpliterator.class);
        assertThat(spliteratorStreamFactory.createStreamFrom(processedEventStreamSpliterator), is(notNullValue()));
    }
}