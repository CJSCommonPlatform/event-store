package uk.gov.justice.services.subscription;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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