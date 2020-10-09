package uk.gov.justice.services.eventsource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.configuration.ContextNameProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEventDestinationResolverTest {

    @Mock
    private ContextNameProvider contextNameProvider;

    @InjectMocks
    private DefaultEventDestinationResolver destinationResolver;

    @Test
    public void shouldReturnDestinationName() {
        assertThat(destinationResolver.destinationNameOf("context1.command.abc"), is("context1.event"));
        assertThat(destinationResolver.destinationNameOf("test.command.bcde"), is("test.event"));
    }

    @Test
    public void shouldUseContextNameIfEventNameStartsWithAdministration() throws Exception {

        final String contextName = "people";
        final String eventName = "administration.management.correction";

        when(contextNameProvider.getContextName()).thenReturn(contextName);

        assertThat(destinationResolver.destinationNameOf(eventName), is("people.event"));
    }
}
