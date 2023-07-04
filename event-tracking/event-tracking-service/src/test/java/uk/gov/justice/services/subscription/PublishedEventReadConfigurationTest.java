package uk.gov.justice.services.subscription;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublishedEventReadConfigurationTest {

    @InjectMocks
    private PublishedEventReadConfiguration publishedEventReadConfiguration;

    @Test
    public void shouldGetTheRangeNormalizationSizeSetByJndi() throws Exception {

        final String rangeNormalizationMaxSize = "23";

        setField(publishedEventReadConfiguration, "rangeNormalizationMaxSize", rangeNormalizationMaxSize);

        assertThat(publishedEventReadConfiguration.getRangeNormalizationMaxSize(), is(23L));
    }
}